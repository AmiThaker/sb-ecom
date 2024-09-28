package com.ecommerce.project.service;

import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartItemRepository;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService{

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;

    @Value("${project.image}")
    private String path;

    @Override
    public ProductDTO addProduct(ProductDTO productDTO, Long categoryId) {

        Product product=modelMapper.map(productDTO,Product.class);

        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","categoryId",categoryId));

        List<Product> products=category.getProducts();
        Product existingProduct=products.stream()
                        .filter(p->p.getProductName().equals(product.getProductName()))
                                .findFirst()
                                        .orElse(null);
        if(existingProduct!=null){
            throw new APIException("Product with product name : "+existingProduct.getProductName()+" already exists!!!");
        }

        product.setImage("default.png");
        product.setCategory(category);
        double specialPrice=product.getPrice()-
                ((product.getDiscount()*0.01)*product.getPrice());
        product.setSpecialPrice(specialPrice);
        Product savedProduct=productRepository.save(product);
        return modelMapper.map(product,ProductDTO.class);
    }

    @Override
    public ProductResponse getAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder=sortOrder.equals("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage=productRepository.findAll(pageDetails);

        List<Product> products=productPage.getContent();

        if(products.size()<1){
            throw new APIException("No Products Added!!!");
        }
        List<ProductDTO> productDTOS=products.stream()
                .map(product->modelMapper.map(product,ProductDTO.class))
                .toList();

        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByCategory(Long categoryId,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Category category=categoryRepository.findById(categoryId)
                .orElseThrow(()->new ResourceNotFoundException("Category","category",categoryId));
        Sort sortByAndOrder=sortOrder.equals("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage=productRepository.findByCategoryOrderByPriceAsc(category,pageDetails);

        List<Product> products=productPage.getContent();
        List<ProductDTO> productDTOS=products.stream()
                .map(product->modelMapper.map(product,ProductDTO.class))
                .toList();
        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductResponse getProductsByKeyword(String keyword,Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder=sortOrder.equals("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();

        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);
        Page<Product> productPage=productRepository.findByProductNameLikeIgnoreCase("%"+keyword+"%",pageDetails);

        List<Product> products=productPage.getContent();
        List<ProductDTO> productDTOS=products.stream()
                .map(product->modelMapper.map(product,ProductDTO.class))
                .toList();



        ProductResponse productResponse=new ProductResponse();
        productResponse.setContent(productDTOS);
        productResponse.setPageNumber(productPage.getNumber());
        productResponse.setPageSize(productPage.getSize());
        productResponse.setTotalElements(productPage.getTotalElements());
        productResponse.setTotalPages(productPage.getTotalPages());
        productResponse.setLastPage(productPage.isLast());
        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(ProductDTO productDTO, Long productId) {
        Product product= modelMapper.map(productDTO,Product.class);
        Product existingProduct=productRepository.findById(productId)
                .orElseThrow(()->new ResourceNotFoundException("Product","product",productId));
        existingProduct.setProductName(product.getProductName());
        existingProduct.setDescription(product.getDescription());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setPrice(product.getPrice());
        double specialPrice=product.getPrice()-
                ((product.getDiscount()*0.01)*product.getPrice());
        existingProduct.setSpecialPrice(specialPrice);

        existingProduct=productRepository.save(existingProduct);

        ProductDTO savedProductDTO=modelMapper.map(existingProduct,ProductDTO.class);
        List<Cart> carts=cartRepository.findCartByProductId(productId);
        List<CartDTO> cartDTOList=carts.stream().map(cart->{
           CartDTO cartDTO=modelMapper.map(cart,CartDTO.class);

           List<ProductDTO> productDTOList=cart.getCartItems().stream()
                   .map(item->modelMapper.map(item.getProduct(),ProductDTO.class)).collect(Collectors.toList());

           cartDTO.setProducts(productDTOList);

           return cartDTO;
        }).collect(Collectors.toList());

        cartDTOList.forEach(cart->cartService.updateProductInCarts(cart.getCartId(),productId));

        return modelMapper.map(savedProductDTO,ProductDTO.class);
    }

    @Override
    public ProductDTO deleteProduct(Long id) {
        Product product=productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product","product",id));
        List<Cart> carts=cartRepository.findCartByProductId(id);
        carts.forEach(cart->cartService.deleteProductFromCart(cart.getCartId(), id));
        productRepository.delete(product);
        ProductDTO productDTO=modelMapper.map(product,ProductDTO.class);
        return productDTO;
    }

    @Override
    public ProductDTO updateProductImage(Long id, MultipartFile image) throws IOException {
        Product existingProduct=productRepository.findById(id)
                .orElseThrow(()->new ResourceNotFoundException("Product","productId",id));

        String fileName=fileService.uploadImage(path,image);

        existingProduct.setImage(fileName);

        Product updatedProduct=productRepository.save(existingProduct);

        ProductDTO updatedProductDTO=modelMapper.map(updatedProduct,ProductDTO.class);
        return updatedProductDTO;
    }


}
