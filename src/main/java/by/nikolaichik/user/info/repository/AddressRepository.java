package by.nikolaichik.user.info.repository;

import by.nikolaichik.user.info.io.entity.AddressEntity;
import by.nikolaichik.user.info.io.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AddressRepository extends CrudRepository<AddressEntity, Long> {

    List<AddressEntity> findAllByUserEntity(UserEntity userEntity);

    AddressEntity findByAddressIdAndUserEntity(String addressId, UserEntity userEntity);
}
