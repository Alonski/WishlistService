# WishlistService

This service is a final project for Cloud Computing Course in Afeka College of Management.

## Team

* Omri Shapira - Team Leader - 203554720
* Gal Odentz - 205892250
* Alon Bukai - 305347007

## Service Description

A simple spring boot service to give a user a way to manage and create Wish list of products he wishes to buy.

The serivce is not reactive, and will use MongoDB as Database, and springboot web.

## Installation

* Running MongoDB local database from the following command: `docker run --name mongotest -p 27017:27017 -d mongo`
* `git clone https://github.com/Omrisha/WishlistService`
* Open project in IntelliJ (Open project menu or import from git menu)
* Open project in Eclipse (File -> Import -> Import existing Gradle Project menu)
* after opening let gradle built and make the project.
* Run (Eclipse -> Run as SpringBoot App, IntelliJ -> via Play button)
* Open http://localhost:{port you defined in application.properties}/swagger-ui.html in your browser.

## Kafka Instructions (macOS/ Linux)
* Use Kafka's quick guide from here: https://kafka.apache.org/quickstart or
* Download From https://apache.mivzakim.net/kafka/2.7.0/kafka_2.13-2.7.0.tgz 
* Open Terminal
* Type `tar -xzf kafka_2.13-2.7.0.tgz`
* Type `cd kafka_2.13-2.7.0`
* Start Zookeeper: `bin/zookeeper-server-start.sh config/zookeeper.properties`
* Open another tab of command line/ Windows Powershell/ Windows Terminal / Terminal (for mac or linux)
* Start Kafka server: `bin/kafka-server-start.sh config/server.properties`
* Open another tab of command line/ Windows Powershell/ Windows Terminal / Terminal (for mac or linux)
* To start sending events type `bin/kafka-console-producer.sh --topic t1 --bootstrap-server localhost:9092`
* Then on the prompt ">" send the wishlist json like: `{"user": {"email":"omri@gmail.com"}, "name":"My awesome things"}`

## Kafka Instructions (Windows)
* Use Kafka's quick guide from here: https://kafka.apache.org/quickstart or
* Download From https://apache.mivzakim.net/kafka/2.7.0/kafka_2.13-2.7.0.tgz 
* Open Command Prompt/ Windows Powershell/ Windows Terminal
* Type `tar -xzf kafka_2.13-2.7.0.tgz`
* Type `cd kafka_2.13-2.7.0`
* Start Zookeeper: `.\bin\windows\zookeeper-server-start.bat .\config\zookeeper.properties`
* Open another tab of Command Prompt/ Windows Powershell/ Windows Terminal
* Start Kafka server: `.\bin\windows\kafka-server-start.bat .\config\server.properties`
* Open another tab of command line/ Windows Powershell/ Windows Terminal
* To start sending events type `.\bin\windows\kafka-console-producer.bat --topic t1 --bootstrap-server localhost:9092`
* Then on the prompt ">" send the wishlist json like: `{"user": {"email":"omri@gmail.com"}, "name":"My awesome things"}`

## Kafka Insturctions (Docker)
* Open Command Prompt/ Windows Powershell/ Windows Terminal / Terminal
* navigate to the WishlishService Root folder
* type: `docker-compose up`
* Open another instance of Command Prompt/ Windows Powershell/ Windows Terminal / Terminal
* (Windows) To start sending events type `.\bin\windows\kafka-console-producer.bat --topic t1 --bootstrap-server localhost:9092`
* (macOS/ Linux) To start sending events type `bin/kafka-console-producer.sh --topic t1 --bootstrap-server localhost:9092`
* Then on the prompt ">" send the wishlist json like: `{"user": {"email":"omri@gmail.com"}, "name":"My awesome things"}`

## JSON Structure

* List:
  user - represent the user that the list belongs to by id (email).
  name - the list name
  id - unique id for the list
  products - list of products in the wishlist
* Product:
  id - unique id for the list
  name - product name
  rating - how users rate the product (can be acheived from Products Reviews Management Service).

## JSON Example
* List boundary when using GET methods
```
{
    "name": "My Wishlist",
    “user”: {
        “email” : “customer12@shop.com”
    },
    “products” : [
        {
           “productId”: “42”,
           "name": "Groot",
           “rating”: 9,
           "price": 94,
           "image": "groot.jpeg",
           "details": { 
                    "manufacturer":  "Marvel", 
                    "collectable": true 
            },
           "category": { 
                    "name":  "Toys", 
                    "description":  "Super heros"
            },
        },
        {
           “productId”: 564”,
           "name": "Baby Yoda",
           “rating”: 3,
           "price": 122,
           "image": "grogu.jpeg",
           "details": { 
                    "manufacturer":  "Star Wars", 
                    "collectable": true 
            },
           "category": { 
                    "name":  "Toys", 
                    "description":  "Super heros"
            },
        }
     ]
}
```
* List JSON when creating new list
```
{
	“user”: {
		“email”: temp@e-commerce.com
	},
	“name”: “My Awesome things”
}
```
* product JSON example for POST/PUT methods
```
{
	“productId”: 526
}
```
* product JSON example for GET methods
```
{
   “productId”: “42”,
   "name": "Groot",
   “rating”: 9,
   "price": 94,
   "image": "groot.jpeg",
   "details": { 
        "manufacturer":  "Marvel", 
        "collectable": true 
    },
   "category": { 
        "name":  "Toys", 
        "description":  "Super heros"
    },
}
```

## API Specification

* POST /wishlist
  - Get new wishlist details and creates it in the DB if not exist already.
  - If the user is not exist in UserManagementService the service will return status code 400.

* GET /wishlist/{email}/{wishlistName}
  - Get wishlist data by its name.
  - If not exist will return status code 404.

* PUT /wishlist/{email}/{wishlistName}
  - Add new product to wishlist by its name and specific user.
  - To get product rating we will inteface the Products Reviews Management Service.
  - If there's no rating the value will be -1.
  - If there's no wishlist with the same name for the user it will return status code 404.

* GET /wishlist?filterBy=productId&filterValue={productId}&sortBy={sortAttrEnum}&sortOrder={sortOrderEnum}&size={size}&page={page}
  - Get all wishlist for the user specified by the email and ordered.
    Order details below.

* GET /wishlist?filterBy=customerEmail&filterValue={customerEmail}&&sortBy={sortAttrEnum}&sortOrder={sortOrderEnum}&size={size}&page={page}
  - Get all wishlist by specific order.
    Order details below.

*  DELETE /wishlist
   - delete all wishlists in the database

* sortAttrEnum can be:
  - user - sort alphabetic by user email
  - name - sort alpabetic by list name
* sortOrderEnum can be:
  - ASC - Ascending order
  - DESC - descending order
 
## API Interfacing:

* ShoppingCatalogService - https://github.com/Omrisha/ShoppingCatalogService
* UserManagementService - https://github.com/Omrisha/user-management-service + https://github.com/Omrisha/dummy-storage-service
* ProductCouponService
