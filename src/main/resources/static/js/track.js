// Live map tracking (Leaflet + OpenStreetMap — free, no API key) + in-app chat
// for the /track/{id} page.
// Expects these globals to already be set by an inline <script> in track.html:
// TRACK_REQUEST_ID, TRACK_SESSION_USER_ID, TRACK_SESSION_ROLE, TRACK_SESSION_NAME,
// TRACK_CUSTOMER_LAT, TRACK_CUSTOMER_LNG, TRACK_HELPER_LAT, TRACK_HELPER_LNG

let map, customerMarker, helperMarker, stompClient;
let watchId = null;
let sharingLocation = false;

const customerIcon = L.icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
});
const helperIcon = L.icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-blue.png',
  shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
  iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
});

function initMap() {
  map = L.map('map').setView([TRACK_CUSTOMER_LAT, TRACK_CUSTOMER_LNG], 13);

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; OpenStreetMap contributors',
  }).addTo(map);

  customerMarker = L.marker([TRACK_CUSTOMER_LAT, TRACK_CUSTOMER_LNG], { icon: customerIcon })
    .addTo(map)
    .bindPopup('Customer (breakdown location)');

  if (TRACK_HELPER_LAT && TRACK_HELPER_LNG) {
    updateHelperMarker(TRACK_HELPER_LAT, TRACK_HELPER_LNG);
  }

  connectWebSocket();
}

function fitToBothMarkers() {
  if (!helperMarker) return;
  const bounds = L.latLngBounds([customerMarker.getLatLng(), helperMarker.getLatLng()]);
  map.fitBounds(bounds, { padding: [50, 50] });
}

function updateHelperMarker(lat, lng) {
  const pos = [lat, lng];
  if (!helperMarker) {
    helperMarker = L.marker(pos, { icon: helperIcon }).addTo(map).bindPopup('Helper (on the way)');
  } else {
    helperMarker.setLatLng(pos);
  }
  fitToBothMarkers();
}

function updateCustomerMarker(lat, lng) {
  if (customerMarker) customerMarker.setLatLng([lat, lng]);
  fitToBothMarkers();
}

function connectWebSocket() {
  const socket = new SockJS('/ws');
  stompClient = new StompJs.Client({
    webSocketFactory: () => socket,
    reconnectDelay: 3000,
    onConnect: () => {
      stompClient.subscribe('/topic/location/' + TRACK_REQUEST_ID, (msg) => {
        const data = JSON.parse(msg.body);
        if (data.role === 'HELPER') updateHelperMarker(data.lat, data.lng);
        else if (data.role === 'CUSTOMER') updateCustomerMarker(data.lat, data.lng);
      });
      stompClient.subscribe('/topic/chat/' + TRACK_REQUEST_ID, (msg) => {
        appendChatMessage(JSON.parse(msg.body));
      });
      stompClient.subscribe('/topic/status/' + TRACK_REQUEST_ID, () => {
        // Status changed (accepted / arrived / completed) — reload so all the
        // server-rendered conditional UI (buttons, rating form, status pill) updates.
        location.reload();
      });
    },
  });
  stompClient.activate();
}

// Manual "Share My Live Location" toggle — works for either role.
function toggleShareLocation() {
  const btn = document.getElementById('shareLocationBtn');
  if (!navigator.geolocation) {
    alert('Geolocation is not supported by this browser.');
    return;
  }

  if (!sharingLocation) {
    watchId = navigator.geolocation.watchPosition(
      (pos) => {
        const lat = pos.coords.latitude;
        const lng = pos.coords.longitude;
        if (TRACK_SESSION_ROLE === 'HELPER') updateHelperMarker(lat, lng);
        else updateCustomerMarker(lat, lng);

        fetch('/api/location/' + TRACK_REQUEST_ID, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ lat, lng }),
        }).catch(() => {});
      },
      (err) => console.warn('Location error:', err.message),
      { enableHighAccuracy: true, maximumAge: 4000, timeout: 10000 }
    );
    sharingLocation = true;
    if (btn) {
      btn.textContent = '🛑 Stop Sharing Location';
      btn.classList.add('btn-danger');
      btn.classList.remove('btn-dark');
    }
  } else {
    if (watchId !== null) navigator.geolocation.clearWatch(watchId);
    sharingLocation = false;
    if (btn) {
      btn.textContent = '📍 Share My Live Location';
      btn.classList.remove('btn-danger');
      btn.classList.add('btn-dark');
    }
  }
}

function appendChatMessage(data) {
  const list = document.getElementById('chatMessages');
  const div = document.createElement('div');
  const mine = String(data.senderId) === String(TRACK_SESSION_USER_ID);
  div.className = 'chat-bubble ' + (mine ? 'chat-mine' : 'chat-theirs');
  const senderLabel = document.createElement('span');
  senderLabel.className = 'chat-sender';
  senderLabel.textContent = data.senderName;
  const body = document.createElement('p');
  body.style.margin = '0';
  body.textContent = data.content;
  div.appendChild(senderLabel);
  div.appendChild(body);
  list.appendChild(div);
  list.scrollTop = list.scrollHeight;
}

function sendChatMessage(event) {
  event.preventDefault();
  const input = document.getElementById('chatInput');
  const content = input.value.trim();
  if (!content || !stompClient || !stompClient.connected) return;
  // Only "content" is sent — the server fills in senderId/senderName from the
  // authenticated session, so a tab can never impersonate another user's name.
  stompClient.publish({
    destination: '/app/chat/' + TRACK_REQUEST_ID,
    body: JSON.stringify({ content: content }),
  });
  input.value = '';
}

function setRatingStars(n) {
  document.getElementById('starsInput').value = n;
  document.querySelectorAll('.star').forEach((el, idx) => {
    el.classList.toggle('star-filled', idx < n);
  });
}
