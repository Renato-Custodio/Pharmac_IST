# PharmacIST
An app to allow users to find pharmacies and medicines in their surroundings, view information about pharmacies and their available stocks, search for medicines at those pharmacies and reserve medicines for future purchase.

## Features tracking
### Core functionality
- [x] **Map with pharmacy locations**
  - [x] Map can be dragged, address searched, or centered on current location
  - [x] Tapping a marker goes to a pharmacy information panel
  - [x] Go to route

- [x] **Medicines**
  - [x] Find medicines (sub-string search)
  - [x] Provide the closest pharmacy with the searched medicine
  - [x] Medicine Information Panel
    - [x] Name, description and picture
    - [x] Pharmacies where available, sorted by distance

- [x] **Account support**
- [x] User Accounts
  - [x] Pharmacy management
    - [x] Add new pharmacy
    - [x] Manage medicine stock
  - [x] Favourite pharmacies
  - [x] Medicine stock notifications
  - [x] Create Medicine
  - [x] User ratings
  - [x] Meta Moderation

### Relevant aspects
- **Resource Frugality and Caching**
  - Implemented LRU Cache for caching map regions of aproximatelly 1.6 squared km
  - Using Picasso library for caching images
  - Implemented search results pagination



## Authors
This project was made by:
- (96925) Gonçalo Abreu Corrêa Brito da Silva
- (110948) Renato Miguel Monteirinho Custódio
- (93747) Pedro Xavier Diogo Gomes

