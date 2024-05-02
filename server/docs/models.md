# Models
## Account
    - GUID
    - username
    - password
    - ownedPharmacies Pharmacy[]
    - favoritePharmacies Pharmacy[]
    - flaggedPharmacies Pharmacy[]
    - subscribedMedicines

## Pharmacy
    - name
    - location (coordinates GPS)
    - picture
    - Medicine[]
    - rating
    - flagCount

## Medicine 
    - name
	- photo
	- stock
	- purpose/preferred Use
	- Pharmacy[]