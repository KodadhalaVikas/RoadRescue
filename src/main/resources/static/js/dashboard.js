// Used on the customer dashboard to capture GPS location before submitting an SOS request.
function submitSosRequest(event) {
  event.preventDefault();
  const form = document.getElementById('sosForm');
  const btn = document.getElementById('sosBtn');
  if (!navigator.geolocation) {
    alert('Geolocation is not supported by this browser.');
    return;
  }
  btn.disabled = true;
  btn.textContent = 'Getting your location...';
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      document.getElementById('latInput').value = pos.coords.latitude;
      document.getElementById('lngInput').value = pos.coords.longitude;
      form.submit();
    },
    (err) => {
      btn.disabled = false;
      btn.textContent = 'Send SOS Request';
      alert('Could not get your location: ' + err.message);
    },
    { enableHighAccuracy: true, timeout: 10000 }
  );
}
