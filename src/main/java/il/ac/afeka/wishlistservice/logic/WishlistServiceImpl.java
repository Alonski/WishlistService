package il.ac.afeka.wishlistservice.logic;

import il.ac.afeka.wishlistservice.boundries.ProductBoundary;
import il.ac.afeka.wishlistservice.boundries.ProductReviewBoundary;
import il.ac.afeka.wishlistservice.boundries.UserBoundary;
import il.ac.afeka.wishlistservice.boundries.WishlistBoundary;
import il.ac.afeka.wishlistservice.data.ProductEntity;
import il.ac.afeka.wishlistservice.data.UserEntity;
import il.ac.afeka.wishlistservice.data.WishlistDao;
import il.ac.afeka.wishlistservice.data.WishlistEntity;
import il.ac.afeka.wishlistservice.enums.FilterByEnum;
import il.ac.afeka.wishlistservice.enums.SortByEnum;
import il.ac.afeka.wishlistservice.enums.SortOrderEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.webjars.NotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class WishlistServiceImpl implements WishlistService {
    private RestTemplate restTemplate;
    private String userServiceUrl;
    private String productsServiceUrl;
    private String reviewsServiceUrl;
    private WishlistDao wishlistDao;

    @Value("${usersService}")
    public void setUserServiceUrl(String userServiceUrl) {
        this.userServiceUrl = userServiceUrl;
    }

    @Value("${productsService}")
    public void setProductsServiceUrl(String productsServiceUrl) {
        this.productsServiceUrl = productsServiceUrl;
    }

    @Value("${reviewsService}")
    public void setReviewsServiceUrl(String reviewsServiceUrl) {
        this.reviewsServiceUrl = reviewsServiceUrl;
    }

    @Autowired
    public WishlistServiceImpl(WishlistDao wishlistDao) {
        this.wishlistDao = wishlistDao;
        this.restTemplate = new RestTemplate();
    }

    @Override
    public WishlistBoundary create(WishlistBoundary wishlist) {
        System.err.println("From Service: " + wishlist);
        if (wishlist.getUser() == null) {
            throw new RuntimeException("User is not defined");
        }
        if (wishlist.getUser().getEmail() == null) {
            throw new RuntimeException("User email is not defined");
        }

        UserBoundary user = getUserByEmail(wishlist.getUser().getEmail());
        if (user == null) {
            throw new RuntimeException("User is not exists.");
        }
        WishlistEntity entity = new WishlistEntity(user.toEntity(), wishlist.getName(), new ArrayList<>());
        WishlistEntity rv = this.wishlistDao.save(entity);
        return new WishlistBoundary(rv);
    }

    @Override
    public WishlistBoundary getWishlistById(String email, String wishListName) {
        if (wishListName == null) {
            throw new RuntimeException("User is not defined");
        }
        if (email == null) {
            throw new RuntimeException("User email is not defined");
        }

        UserBoundary user = getUserByEmail(email);
        if (user == null) {
            throw new RuntimeException("User is not exists.");
        }
        WishlistEntity rv = this.wishlistDao.findById(email + WishlistEntity.KEY_DELIMETER + wishListName).orElse(null);
        if (rv == null)
            throw new RuntimeException("Wishlist with the name " + wishListName + " is not exist for " + email);
        WishlistBoundary boundary = new WishlistBoundary(rv);
        boundary.setProducts(rv.getProducts().stream().map(p -> getProductById(p.getProductId())).collect(Collectors.toList()));
        return boundary;
    }

    @Override
    public void addProduct(String email, String wishListName, ProductBoundary productBoundary) {
        if (wishListName == null) {
            throw new RuntimeException("User is not defined");
        }
        if (email == null) {
            throw new RuntimeException("User email is not defined");
        }

        ProductBoundary product = getProductById(productBoundary.getProductId());
        if (product == null) {
            throw new RuntimeException("Product is not exists.");
        }
        WishlistEntity wishlistToUpdate = this.wishlistDao.findById(email + WishlistEntity.KEY_DELIMETER + wishListName).orElse(null);
        if (wishlistToUpdate == null)
            throw new RuntimeException("Wishlist with the name " + wishListName + " is not exist for " + email);

        ProductEntity entity = new ProductEntity(product.getProductId());
        //entity.setRating(getRatingByProductId(product.getProductId()).getRating());
        wishlistToUpdate.addProduct(entity);
        this.wishlistDao.save(wishlistToUpdate);
    }

    @Override
    public WishlistBoundary[] getAll(FilterByEnum filterBy, String filterValue, SortByEnum sortBy, SortOrderEnum sortOrder, int size, int page) {
        List<WishlistEntity> wishlists = new ArrayList<>();
        Sort.Direction dir = sortOrder.equals(SortOrderEnum.DESC) ? Sort.Direction.DESC : Sort.Direction.ASC;

        switch (filterBy) {
            case customerEmail:
                wishlists = this.wishlistDao.findAllByUser(new UserEntity(filterValue), PageRequest.of(page, size, dir, sortBy.toString()));
                break;
            case productId:
                wishlists = this.wishlistDao.findAll(PageRequest.of(page, size, dir, sortBy.toString())).getContent();
                wishlists = wishlists.stream().filter(w -> w.getProducts().contains(new ProductEntity(filterValue))).collect(Collectors.toList());
                break;
            default:
                wishlists = this.wishlistDao.findAll(PageRequest.of(page, size, dir, sortBy.toString())).getContent();
                break;
        }

        List<WishlistBoundary> wishlistBoundaries = new ArrayList<>();
        wishlistBoundaries = wishlists.stream().map(WishlistBoundary::new).collect(Collectors.toList());
        List<WishlistEntity> finalWishlists = wishlists;
        wishlistBoundaries.forEach(wb -> {
            finalWishlists.forEach(w -> wb.setProducts(w.getProducts().stream().map(p -> getProductById(p.getProductId())).collect(Collectors.toList())));
        });

        return (WishlistBoundary[])wishlistBoundaries.toArray();
    }

    @Override
    public void deleteAll() {
        this.wishlistDao.deleteAll();
    }

    private UserBoundary getUserByEmail(String email) {
        try {
            return this.restTemplate.getForObject(
                    this.userServiceUrl + "/{email}",
                    UserBoundary.class,
                    email);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    private ProductBoundary getProductById(String productId) {
        try {
            return this.restTemplate.getForObject(
                    this.productsServiceUrl + "/products/{productId}",
                    ProductBoundary.class,
                    productId);
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
    private ProductReviewBoundary getRatingByProductId(String productId) {
        try {
            ProductReviewBoundary product = this.restTemplate.getForObject(
                    this.reviewsServiceUrl + "/average_rating/{productId}",
                    ProductReviewBoundary.class,
                    productId);

            if (product == null)
                return new ProductReviewBoundary(productId, -1);
            return product;
        } catch (Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }
}
