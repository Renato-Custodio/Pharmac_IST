# PharmacIST
An app to allow users to find pharmacies and medicines in their surroundings, view information about pharmacies and their available stocks, search for medicines at those pharmacies and reserve medicines for future purchase.

## Backend setup
Local deployment of the backend structure (mongodb + NestJS server) can be done using the docker compose located in the server folder.
- Start server and database command
    ```
    docker-compose up -d
    ```
- Stop command
    ```
    docker-compose down
    ```

## Features tracking
### Core functionality
- [x] **Map with pharmacy locations**
  - [x] Map can be dragged, address searched, or centered on current location
  - [x] Tapping a marker goes to a pharmacy information panel

- [x] **Medicines**
  - [x] Find medicines (sub-string search)
  - [x] Provide the closest pharmacy with the searched medicine
  - [x] Medicine Information Panel
    - [x] Name, description and picture
    - [x] Pharmacies where available, sorted by distance

- [ ] **Account support**
  - [ ] Pharmacy management
    - [ ] Add new pharmacy
    - [ ] Manage medicine stock
  - [ ] Favourite pharmacies
  - [ ] Medicine stock notifications

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

