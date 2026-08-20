package org.example.stocktradingplatform.Service;


import org.example.stocktradingplatform.Dto.Mapper.UserMapper;
import org.example.stocktradingplatform.Dto.RequestResponse.UserRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.UserResponseDto;
import org.example.stocktradingplatform.Entity.Userr;
import org.example.stocktradingplatform.ExceptionManager.UserrNotFind;
import org.example.stocktradingplatform.Reposity.UserReposity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserReposity userReposity;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final org.example.stocktradingplatform.Reposity.BasketReposity basketReposity;
    private final org.example.stocktradingplatform.Reposity.BasketItemRepository basketItemRepository;
    private final org.example.stocktradingplatform.Reposity.FavoriteReposity favoriteReposity;
    private final org.example.stocktradingplatform.Reposity.FavoriteProductRepository favoriteProductRepository;
    private final org.example.stocktradingplatform.Reposity.ProductReposity productReposity;

    public UserService(UserMapper userMapper,
                       UserReposity userReposity,
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                       org.example.stocktradingplatform.Reposity.BasketReposity basketReposity,
                       org.example.stocktradingplatform.Reposity.BasketItemRepository basketItemRepository,
                       org.example.stocktradingplatform.Reposity.FavoriteReposity favoriteReposity,
                       org.example.stocktradingplatform.Reposity.FavoriteProductRepository favoriteProductRepository,
                       org.example.stocktradingplatform.Reposity.ProductReposity productReposity) {
        this.userMapper = userMapper;
        this.userReposity = userReposity;
        this.passwordEncoder = passwordEncoder;
        this.basketReposity = basketReposity;
        this.basketItemRepository = basketItemRepository;
        this.favoriteReposity = favoriteReposity;
        this.favoriteProductRepository = favoriteProductRepository;
        this.productReposity = productReposity;
    }

    public UserResponseDto createUser(UserRequestDto requestDto) {
        Userr user = userMapper.toEntity(requestDto);
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == null) {
            user.setRole(org.example.stocktradingplatform.Entity.Role.ROLE_USER);
        }
        Userr userSave = userReposity.save(user);
        return userMapper.toUserResponseDto(userSave);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteUser(Long id) {
        Userr user = userReposity.findById(id).orElseThrow(() -> new UserrNotFind("User not found with id: " + id));

        if ("admin@stock.com".equalsIgnoreCase(user.getEmail()) || user.getRole() == org.example.stocktradingplatform.Entity.Role.ROLE_ADMIN) {
            throw new RuntimeException("System Administrator account cannot be deleted");
        }

        // Delete favorites
        if (user.getFavorite() != null) {
            favoriteProductRepository.deleteByFavoriteId(user.getFavorite().getId());
            favoriteReposity.delete(user.getFavorite());
            user.setFavorite(null);
        }

        // Delete basket
        if (user.getBasket() != null) {
            basketItemRepository.deleteByBasketId(user.getBasket().getId());
            basketReposity.delete(user.getBasket());
            user.setBasket(null);
        }

        // Unlink products if any
        if (user.getProducts() != null) {
            for (org.example.stocktradingplatform.Entity.Product p : user.getProducts()) {
                p.setUserr(null);
                productReposity.save(p);
            }
        }

        userReposity.delete(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public UserResponseDto UpdateUser(Long id, UserRequestDto requestDto) {
        Userr updateUser = userReposity.findById(id).orElseThrow(() -> new UserrNotFind("User not found with id: " + id));
        if (requestDto.getName() != null) updateUser.setName(requestDto.getName());
        if (requestDto.getSurname() != null) updateUser.setSurname(requestDto.getSurname());
        if (requestDto.getAge() != null) updateUser.setAge(requestDto.getAge());
        if (requestDto.getPhoneNumber() != null) updateUser.setPhoneNumber(requestDto.getPhoneNumber());
        if (requestDto.getEmail() != null) updateUser.setEmail(requestDto.getEmail());
        if (requestDto.getBalance() != null) updateUser.setBalance(requestDto.getBalance());
        if (requestDto.getPassword() != null && !requestDto.getPassword().trim().isEmpty()) {
            updateUser.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        }

        Userr savedUser = userReposity.save(updateUser);
        return userMapper.toUserResponseDto(savedUser);
    }
    public  List<UserResponseDto> getAllUser(){
        List<UserResponseDto> list= userReposity.findAll().stream()
                .map(userr -> userMapper.toUserResponseDto(userr))
                .toList();
        return list;
    }
    public  UserResponseDto getByIdUser(Long id){
        Userr user = userReposity.findById(id).orElseThrow(() -> new UserrNotFind(" User not find  :("));
        return   userMapper.toUserResponseDto(user);
    }




}
