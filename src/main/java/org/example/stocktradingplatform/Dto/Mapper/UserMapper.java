package org.example.stocktradingplatform.Dto.Mapper;


import org.example.stocktradingplatform.Dto.RequestResponse.UserRequestDto;
import org.example.stocktradingplatform.Dto.RequestResponse.UserResponseDto;
import org.example.stocktradingplatform.Entity.Userr;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    Userr toEntity(UserRequestDto  dto);
    UserResponseDto toUserResponseDto(Userr userr);


}
