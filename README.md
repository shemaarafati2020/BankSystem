
<h1 align="center">💳 MyBank System</h1>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?logo=java&logoColor=white" />
  <img src="https://img.shields.io/badge/Database-MySQL-blue?logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/UI-Java%20Swing-green?logo=appveyor&logoColor=white" />
  <img src="https://img.shields.io/badge/License-MIT-yellow?logo=open-source-initiative&logoColor=white" />
</p>

<p align="center">
  <b>A modern banking system built with Java Swing and MySQL — designed for simplicity, speed, and security.</b>
</p>

---

## 🎨 Project Overview

**MyBank System** is a sleek, LAN-compatible **desktop banking application** built with **Java Swing**.  
It provides users with intuitive access to core financial features like **Deposits, Withdrawals, Transfers, and Statements** — all managed through a responsive dashboard interface.

<p align="center">
  <img src="https://www.google.com/url?sa=i&url=https%3A%2F%2Fdribbble.com%2Ftags%2Fmobile-banking&psig=AOvVaw3WohmFzCpvM5pPFDkPTRCQ&ust=1761738142199000&source=images&cd=vfe&opi=89978449&ved=0CBUQjRxqFwoTCPi8-ZroxpADFQAAAAAdAAAAABAE"/>
</p>

---

## 🌈 Design Highlights

🧭 **Interface Theme:**  
Minimalist teal-gray theme inspired by modern fintech dashboards.  

🎨 **Color Palette:**
| Element | Color | Hex |
|----------|--------|-----|
| Primary Background | Deep Teal | `#2F3E46` |
| Accent | Soft Olive | `#CAD2C5` |
| Buttons | Slate Blue | `#52796F` |
| Text | Ivory White | `#F8F9FA` |

🖋️ **Typography:**  
Uses “Segoe UI” — clean and legible across all resolutions.  

🧩 **UI Features:**
- 🪟 Rounded corners and smooth hover effects  
- 🖼️ Circular user avatar rendering  
- 📊 Dashboard widgets (Balance, Transaction Cards, etc.)  
- 💡 Dynamic refresh and event-based updates  

---

## 🧱 Architecture Overview

```plaintext
com.mybank
├── dao
│   ├── DBConnection.java         # Database connector
│   ├── UserDAO.java              # Handles user CRUD
│   └── TransactionDAO.java       # Handles transactions
│
├── model
│   ├── User.java                 # User entity
│   └── Transaction.java          # Transaction entity
│
├── view
│   ├── LoginFrame.java
│   ├── RegisterFrame.java
│   ├── DashboardFrame.java
│   ├── DepositFrame.java
│   ├── WithdrawFrame.java
│   ├── TransferFrame.java
│   ├── TransactionHistoryFrame.java
│   └── ProfileFrame.java
│
└── Main.java                     # App entry point
````

---

## 🧠 Core Features

| Feature                              | Description                                                 |
| ------------------------------------ | ----------------------------------------------------------- |
| 🔐 **Secure Login / Registration**   | BCrypt-based password hashing for safe credentials.         |
| 💰 **Deposit / Withdraw / Transfer** | Full transaction management with validation.                |
| 📜 **Transaction History**           | Track your activity with timestamps and filtering.          |
| 🖼️ **Profile Customization**        | Upload avatar + update personal info.                       |
| 📈 **Dashboard Insights (Pro)**      | Real-time balance, last 5 transactions, and activity chart. |
| 🧾 **Export Statements (PDF)**       | Generate financial summaries for offline records.           |

---

## 🗃️ Database Schema

### 🧍 Table: `users`

| Column     | Type                 | Details             |
| ---------- | -------------------- | ------------------- |
| user_id    | INT (PK, AI)         | Unique user ID      |
| username   | VARCHAR(50)          | Unique username     |
| password   | VARCHAR(255)         | Hashed password     |
| full_name  | VARCHAR(100)         | User's full name    |
| email      | VARCHAR(100)         | User's email        |
| phone      | VARCHAR(20)          | Contact number      |
| address    | VARCHAR(255)         | Residential address |
| photo      | VARCHAR(255)         | File path to photo  |
| role       | ENUM('user','admin') | User access level   |
| created_at | TIMESTAMP            | Auto timestamp      |

### 💳 Table: `transactions`

| Column         | Type                                  | Details                         |
| -------------- | ------------------------------------- | ------------------------------- |
| id             | INT (PK, AI)                          | Transaction ID                  |
| user_id        | INT                                   | Linked to `users.user_id`       |
| type           | ENUM('DEPOSIT','WITHDRAW','TRANSFER') | Transaction type                |
| amount         | DECIMAL(12,2)                         | Transaction amount              |
| target_account | VARCHAR(50)                           | Target username (for transfers) |
| created_at     | TIMESTAMP                             | Auto timestamp                  |

---

## 💻 How to Run Locally

### 1️⃣ Clone Repository

```bash
git clone https://github.com/yourusername/MyBankSystem.git
cd MyBankSystem
```

### 2️⃣ Create Database

```sql
CREATE DATABASE mybank_system;
USE mybank_system;
```

Import schema (from the section above).

### 3️⃣ Configure Database Connection

Edit file: `DBConnection.java`

```java
private static final String URL = "jdbc:mysql://localhost:3306/mybank_system?useSSL=false&serverTimezone=UTC";
private static final String USER = "root";
private static final String PASSWORD = "";
```

### 4️⃣ Build & Run

```bash
mvn clean install
java -cp target/classes com.mybank.Main
```

✅ Or simply open in **IntelliJ IDEA** → Run ▶️ `Main.java`.

---

## 🧑‍💼 User Roles

### 👤 **Standard User**

* Perform transactions
* View personal history
* Edit profile details

### 🧰 **Admin (Pro Mode)**

* View all users
* Export global transaction reports
* Manage users and roles

---

## 🛠️ Tech Stack

| Layer        | Technology                                 |
| ------------ | ------------------------------------------ |
| **Frontend** | Java Swing (AWT, FlatLaf-inspired styling) |
| **Backend**  | Java DAO Pattern                           |
| **Database** | MySQL / MariaDB                            |
| **Security** | BCrypt (Password hashing)                  |
| **Reports**  | OpenPDF (PDF Export)                       |

---

## 🚀 Upcoming Features

* 🧮 Balance graphs with Recharts
* 🧾 Automated monthly statement emails
* 🌐 RESTful API for mobile app integration
* 🧑‍💻 Dark mode UI theme
* 🪙 Currency conversion widget

---

## ✨ Credits

Developed with ❤️ by **shemaarafati2020**

> *“Simplicity, Security, and Speed — That’s MyBank.”*

📧 Contact: **[shemaarafati26@gmail.com](mailto:shemaarafati26@gmail.com)**
🌍 Based in: **Rwanda 🇷🇼**
🧑‍💻 GitHub: [@shemaarafati2020](https://github.com/shemaarafati2020)

---

<p align="center">
  <sub>© 2025 MyBank System | Designed for Desktop Banking</sub>
</p>
```


Would you like me to **generate realistic mock screenshots** (Swing UI in teal/gray theme) for your actual project — Login, Dashboard, Deposit, etc.?
I can create those visuals for you automatically so they match your project’s design.
