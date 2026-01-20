# NEOKey (Android Authenticator)

# Pages: https://maximpollak.github.io/CCL3-WS2025/#usability

## Brief description
NEOKey is an offline-first Android authentication app that securely stores and manages secrets and generated passwords. All sensitive data is encrypted at rest and can be unlocked using biometrics or a master pincode.

## Team members
Timon Schneider - cc241026
Maxim Pollák - cc241059

## App concept

### Goal
Provide a fast, simple, and secure way to store and use authentication codes and passwords **without requiring internet access**, while keeping sensitive data **encrypted** and protected by **biometric unlock** and a master pincode. 

### Use case
A user wants to:
- store multiple authentication entries (e.g., services, accounts, shared secrets, passwords),
- organize them in categories,
- quickly reveal/hide codes when needed,
- generate strong passwords inside the app,
- (Nice-to-Have) Share passwords with others.

### Target user
- Students and everyday users who manage many accounts and want a secure offline solution.
- Security-conscious users who prefer local storage and encryption.
- Users who want quick access with biometric unlock (fingerprint/face).
- User who want the option to share passwords securely.

## Hi-Fi Prototype (User Flow)
**Prototype link:** https://www.figma.com/proto/DbWLH6FjqRhIzv348Sdi6t/CCL_Prototype?page-id=8%3A2&node-id=13-2&p=f&viewport=-571%2C-171%2C0.36&t=QjuQRp3v8rH6H9SL-1&scaling=min-zoom&content-scaling=fixed


### App Launch → Unlock
The app opens with biometric authentication (fingerprint/face).  
A fallback PIN/password is available if biometrics fail.

### Home – Entries List
The user sees all encrypted entries, each showing:
- Service / label
- Category

Quick actions:
- Reveal / Hide code
- Search
- Filter by category
- Add new entry

### Entry Details
Displays:
- Service / label
- Username (optional)
- Secret / code (hidden by default)
- Password strength indicator
- Notes (optional)

Actions:
- Reveal / Hide
- Copy
- Edit
- Delete

### Add / Edit Entry (Wizard Form)
Entries are created or modified using a step-by-step wizard.

Fields:
- Label / service name  
- Secret / code (with live strength meter)  
- Category  
- Notes (optional)

### Password Generator
Generates secure passwords with adjustable options (length, symbols, numbers).

Features:
- Strength meter (weak → medium → strong)
- Copy password
- Insert into entry form


### Core features
- Full CRUD: Create / Read / Update / Delete encrypted code entries
- Local SQLite database (Room)
- Jetpack Compose UI (no XML)
- Reveal/Hide stored codes
- Built-in secure password generator
- Password strength meter (weak → medium → strong)
- Offline-first design (no internet required)
- Biometric unlock (fingerprint / face)
- Encryption & decryption
- Categories for organization


## Tech stack
- Kotlin
- Jetpack Compose
- Room (SQLite)
- Offline-first architecture
- Encryption layer (e.g., Android Keystore + encrypted data)

