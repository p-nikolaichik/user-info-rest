package by.nikolaichik.user.info.shared.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonProperty.Access.WRITE_ONLY;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    @JsonIgnore
    private long id;

    private String userId;

    private String firstName;

    private String lastName;

    private String email;

    @JsonProperty(access = WRITE_ONLY)
    private String password;

    private List<AddressDTO> addresses;

    @JsonIgnore
    private String emailVerificationToken;

    @JsonIgnore
    private Boolean emailVerificationStatus = false;

}
