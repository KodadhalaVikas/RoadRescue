RoadRescue connects a stranded driver (**Customer**) with a nearby mechanic/two operator (**Helper**) in real time as a **Spring Boot + MySQL + HTML/CSS/JS** web application, named **RoadRescue**, with a designed professional UI and three main features:
1. **Live map tracking** — the helper's pin moves on the customer's map in real time (Google Maps JS API + WebSocket).
2. **In-app chat** — customer and helper message each other directly on the request page (WebSocket/STOMP).
3. **Ratings & reviews** — customers rate helpers after a completed job; each helper's average rating is shown.

---

## 1. Tech stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Backend framework | Spring Boot 3.3 (Web, Thymeleaf, Data JPA, WebSocket, Validation) |
| Database | MySQL 8 |
| Real-time | Spring WebSocket + STOMP + SockJS |
| Frontend | Thymeleaf templates + hand-written CSS + vanilla JavaScript |
| Maps | Google Maps JavaScript API |
| Build tool | Maven |
| Editor | VS Code |

A server-rendered web app you run with `mvn spring-boot:run` and open in a browser.

---

## 2. Project structure

```
RoadRescue/
├── pom.xml
├── src/main/java/com/roadrescue/
│   ├── RoadRescueApplication.java        # entry point
│   ├── config/
│   │   ├── WebSocketConfig.java          # STOMP/SockJS setup
│   │   └── HttpSessionHandshakeInterceptor.java  # verifies chat sender identity
│   ├── model/                            # JPA entities: User, HelpRequest, ChatMessage, Rating
│   ├── repository/                       # Spring Data JPA repositories
│   ├── service/                          # business logic (auth, requests, ratings)
│   └── controller/                       # AuthController, CustomerController, HelperController,
│                                          # TrackingController, LocationRestController,
│                                          # ChatWebSocketController, RatingController
├── src/main/resources/
│   ├── application.properties.example    # copy this → application.properties
│   ├── templates/                        # index, login, signup, dashboards, track.html
│   └── static/
│       ├── css/style.css                 # the full UI redesign
│       └── js/                           # dashboard.js, track.js
└── README.md
```

---

## 3. Prerequisites (install these first)

1. **JDK 17** — https://adoptium.net/ (verify with `java -version`)
2. **Maven** — https://maven.apache.org/download.cgi (verify with `mvn -version`)
3. **MySQL Server 8** — https://dev.mysql.com/downloads/mysql/ (or MySQL Workbench for a GUI)
4. **VS Code** with these extensions:
   - "Extension Pack for Java" (Microsoft)
   - "Spring Boot Extension Pack" (VMware/Microsoft)

Maps are powered by **Leaflet + OpenStreetMap** — no Google account, API key, or billing setup needed. Nothing to configure here.

---

## 4. Local setup, step by step

### Step 1 — Create the MySQL database
Open a terminal (or MySQL Workbench) and run:
```sql
CREATE DATABASE roadrescue_db;
```
(You can skip this — the app is configured with `createDatabaseIfNotExist=true` and will
create it automatically on first run. Creating it manually just lets you confirm your
MySQL credentials work first.)

### Step 2 — Configure the app
In `src/main/resources/`, copy the example file:
```bash
cp application.properties.example application.properties
```
Open `application.properties` and fill in:
```properties
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```
`application.properties` is already in `.gitignore` — it will never be pushed to GitHub,
so your real password stays private.

### Step 3 — Open in VS Code
```bash
cd RoadRescue
code .
```
VS Code will detect the Maven project and download dependencies automatically (first time
only — this needs internet access).

### Step 4 — Run the app
Either:
- Press `F5` / use the **Spring Boot Dashboard** (left sidebar → RoadRescue → ▶ Run), or
- From the integrated terminal:
```bash
mvn spring-boot:run
```
On first run, Hibernate will auto-create all the MySQL tables (`users`, `help_requests`,`chat_messages`, `ratings`) inside `roadrescue_db`.

### Step 5 — Open the app
Go to **http://localhost:8080**

### Step 5 — Test the full flow

⚠️ **Important:** RoadRescue logs you in with a browser cookie (a session), and **all tabs in the same browser share the same cookie jar**. If you open the Customer in one tab and the Helper in another tab of the *same* browser, logging into the second one will silently
log the first one out and swap its identity too — you'll see chat messages and the wrong dashboard attributed to whichever account you logged into last. **Always test the two roles in two separate browser contexts**, for example:
- Chrome **normal window** for the Customer + Chrome **Incognito window** for the Helper, or
- Chrome for one role + Edge/Firefox for the other.

Now the actual flow:
1. Window A: Sign up as a **Customer**.
2. Window B: Sign up as a **Helper**.
3. Window A (Customer): fill in the issue, click **Send SOS Request** → allow location access.
4. Window B (Helper): refresh the dashboard, click **Accept** on the new request.
5. Both windows: open the request's **Track** page — you should see an OpenStreetMap map
   load immediately (no key needed). Click **📍 Share My Live Location** in either window
   (browser will ask for location permission) — that pin will move live on the other
   window's map over the WebSocket connection. Try the chat box in both windows too.
6. Window B (Helper): click **Mark as Arrived**, then **Mark Job Completed**.
7. Window A (Customer): the rating form appears — submit stars + a review.

If all of that works, the app is running correctly on your machine.
---

## 6. Troubleshooting

- **"Access denied for user 'root'@'localhost'"** → your MySQL password in
  `application.properties` is wrong.
- **Both tabs show the same logged-in name / chat messages all show one person's name /
  rating form never appears for the customer** → you're testing both roles in the same browser, so they share one session cookie. Use two separate browser contexts (see Step 6).
- **Map tiles don't load** → OpenStreetMap needs an internet connection to fetch map tiles
  (they load from `tile.openstreetmap.org`); this is unrelated to your local MySQL/Java setup.
- **A pin never moves** → click **📍 Share My Live Location** in that person's window and accept the browser's location permission prompt — location is only sent while sharing is turned on and that browser tab stays open.
- **Port 8080 already in use** → change `server.port` in `application.properties`.


## 7. ScreenShots

## Customer Registration Page

<img width="960" height="540" alt="Customer Registration Page" src="https://github.com/user-attachments/assets/2c133f13-def4-4591-a9f2-2229bd25a6f2" />

## Helper Registration Page

<img width="960" height="540" alt="Helper Registration Page" src="https://github.com/user-attachments/assets/13134779-bd0e-4dd1-928f-9e337479120e" />

## Login Page

<img width="960" height="540" alt="Login Page" src="https://github.com/user-attachments/assets/a42c3258-6a3d-4ed3-8335-26499634aa69" />

## Customer Dashboard

<img width="960" height="540" alt="Customer Dashboard" src="https://github.com/user-attachments/assets/c638dafd-2e01-49ba-ba6a-20e3affdce69" />

## Helper Dashboard

<img width="960" height="540" alt="Helper Dashboard" src="https://github.com/user-attachments/assets/8f24357d-042f-485f-a4ae-069472664abf" />

## Customer request

<img width="960" height="540" alt="Customer Request" src="https://github.com/user-attachments/assets/1cf852de-7acf-4bc2-976c-40210f5fa668" />

## Helper response

<img width="960" height="540" alt="Helper Response" src="https://github.com/user-attachments/assets/57600b8f-439d-48ce-ad1a-8e5fefc61f10" />

## Customer Chat

<img width="960" height="540" alt="Customer Chat" src="https://github.com/user-attachments/assets/3ca28673-cc8f-4ffe-b782-ef5ff27e916a" />

## Helper chat

<img width="960" height="540" alt="Helper Chat" src="https://github.com/user-attachments/assets/3b36aad2-40da-4c7c-b584-ba7208558409" />

## Customer Delivery Completed 

<img width="960" height="540" alt="Customer Delivery Completed" src="https://github.com/user-attachments/assets/53208d20-4c5d-4379-bf57-3ebb570928cd" />

## Helper delivery Completed

<img width="960" height="540" alt="Helper Delivery Completed" src="https://github.com/user-attachments/assets/8dde7080-e188-4fe6-b382-bc028975e2e2" />

## Customer Rating

<img width="960" height="540" alt="Customer Rating" src="https://github.com/user-attachments/assets/4210f471-bcd5-449e-841e-11c43e13d07b" />

## Backend

<img width="960" height="540" alt="Backend" src="https://github.com/user-attachments/assets/7c1dd1ee-ac9f-4255-bc71-df1f3fb9837d" />
