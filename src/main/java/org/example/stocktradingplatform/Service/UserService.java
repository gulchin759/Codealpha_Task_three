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

    public UserService(UserMapper userMapper, UserReposity userReposity) {
        this.userMapper = userMapper;
        this.userReposity = userReposity;

    }

    public UserResponseDto createUser(UserRequestDto requestDto) {
        Userr user = userMapper.toEntity(requestDto);
        Userr userSave = userReposity.save(user);
        return userMapper.toUserResponseDto(userSave);
    }




    public void deleteUser(Long id) {
        Userr user = userReposity.findById(id).orElseThrow(() -> new UserrNotFind("User not find and delete fail :("));
        userReposity.delete(user);
    }

    public UserResponseDto UpdateUser(Long id, UserRequestDto requestDto) {
        Userr user = userMapper.toEntity(requestDto);
        Userr updateUser = userReposity.findById(id).orElseThrow(() -> new UserrNotFind(" User not find and update  fail :("));
        updateUser.setName(user.getName());
        updateUser.setSurname(user.getSurname());
        updateUser.setAge(user.getAge());
        updateUser.setEmail(user.getEmail());
        updateUser.setBalance(user.getBalance());
        updateUser.setPassword(user.getPassword());
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
