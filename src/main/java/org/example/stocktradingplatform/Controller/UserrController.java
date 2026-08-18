package org.example.stocktradingplatform.Controller;


import org.example.stocktradingplatform.Dto.RequestResponse.UserRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.UserResponseDto;
import org.example.stocktradingplatform.Service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserrController {

    private  final UserService userService;

    public UserrController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserResponseDto createUser(@RequestBody UserRequestDto requestDto){
        return  userService.createUser(requestDto);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
    }

    @PutMapping("/{id}")
    public  UserResponseDto UpdateUser(@PathVariable Long id, @RequestBody UserRequestDto requestDto){
        return userService.UpdateUser(id,requestDto);
    }

    @GetMapping
    public List<UserResponseDto> getAllUser(){
        return userService.getAllUser();
    }

    @GetMapping("/{id}")
    public  UserResponseDto getByIdUser(@PathVariable Long id){
        return  userService.getByIdUser(id);
    }


}
