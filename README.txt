# EventEcho Project

**Name:** Elizabeth Rakhi Baby

## Technologies Used
- **Frontend:** React, JSF, PrimeFaces
- **Backend:** Java EE
- **Database:** Java DB

## Project Description
This event management system enables seamless interaction between event organizers and attendees, following the MVC pattern. Java EE powers the backend, while JSF and PrimeFaces provide a dynamic, user-friendly interface. JSF managed beans handle data processing and user input.

In a later redevelopment phase, RESTful APIs were introduced to connect a new React frontend with the existing Java EE backend, demonstrating full-stack development expertise and effective integration techniques.

## Deployment Instructions (Standard)
1. Create a Java DB database named **"eventechoDB"** and connect to it.
2. Start the GlassFish Server.
3. Open the project in NetBeans.
4. Select **"Clean & Build"**, then **"Deploy"**.
5. Click **"Run"** in NetBeans.
6. Access the webpage that appears in your browser.

## Deployment Instructions (Alternative)
1. Create a Java DB database named **"eventechoDB"** and connect to it.
2. Start the GlassFish Server.
3. Open the project in NetBeans.
4. Select **"Clean & Build"**, then **"Deploy"**.
5. Open the **"scrm"** folder in Visual Studio Code.
6. In the VS Code terminal, run:
   npm run build
   npm run start
7. Access the webpage that appears in your browser.

## Entities
- Person
- Event
- Registration
