package by.nikolaichik.user.info.shared;

import by.nikolaichik.user.info.shared.dto.UserDTO;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;
import org.springframework.stereotype.Component;

@Component
public class AmazonSES {

    final String FROM = "p.nikolaichik@gmail.com";

    final String SUBJECT = "One last step to complete your registration with PhotoApp";

    final String PASSWORD_RESET_SUBJECT = "Password reset request";

    final String HTMLBODY = "<h1>Please verify your email registration with PhotoApp<h1>"
            + "<p>Thank you for registering with our mobile app. To complete registration process"
            + "click on the following link: "
            + "<a href='http://ec2-54-161-102-60.compute-1.amazonaws.com:8080/verification-service/email-verification.jsp?token=$tokenValue'>"
            + "final step to complete your registration" + "</a><br/><br/>"
            + "Thank you! And we are waiting for you inside!";

    final String TEXTBODY = "Please verify your email registration with PhotoApp<h1>"
            + "Thank you for registering with our mobile app. To complete registration process"
            + "open then the following URL in your browser window: "
            + "http://ec2-54-161-102-60.compute-1.amazonaws.com:8080/verification-service/email-verification.jsp?token=$tokenValue"
            + "final step to complete your registration"
            + "Thank you! And we are waiting for you inside!";

    final String PASSWORD_RESET_HTMLBODY = "<h1>A request to reset your password</h1>"
            + "<p>Hi, $firstName!</p>"
            + "<p>Someone has requested to reset your password with our project. If it were not you, please don't react to this message"
            + ", otherwise plase click on the link below to set a new password: "
            + "<a href='http://localhost:8080/verification-service/password-reset.jsp?token=$tokenValue'>"
            + "Click this line to Reset Password"
            + "</a><br/><br/>"
            + "Thank you!";

    final String PASSWORD_RESET_TEXTBODY = "A request to reset your password"
            + "Hi, $firstName!"
            + "Someone has requested to reset your password with our project. If it were not you, please don't react to this message"
            + ", otherwise plase click on the link below to set a new password: "
            + "http://localhost:8080/verification-service/password-reset.jsp?token=$tokenValue"
            + "Thank you!";

    final String ACCESS_KEY = "";

    final String SECRET_KEY = "";

    private AmazonSimpleEmailService getAmazonSimpleEmailService() {
        BasicAWSCredentials credentials = new BasicAWSCredentials(ACCESS_KEY, SECRET_KEY);
        return AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(credentials)).withRegion(Regions.US_EAST_1).build();
    }

    public void verifyEmail(UserDTO userDTO) {
        AmazonSimpleEmailService client = getAmazonSimpleEmailService();

        String htmlBodyWithToken = HTMLBODY.replace("$tokenValue", userDTO.getEmailVerificationToken());
        String textBodyWithToken = TEXTBODY.replace("$tokenValue", userDTO.getEmailVerificationToken());

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(userDTO.getEmail()))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(SUBJECT)))
                .withSource(FROM);

        client.sendEmail(request);
        System.out.println("Email sent!");
    }

    public boolean sendPasswordResetRequest(String firstName, String email, String token) {
        boolean returnValue = false;
        AmazonSimpleEmailService client = getAmazonSimpleEmailService();

        String htmlBodyWithToken = PASSWORD_RESET_HTMLBODY.replace("$tokenValue", token);
        htmlBodyWithToken = htmlBodyWithToken.replace("$firstName", firstName);
        String textBodyWithToken = PASSWORD_RESET_TEXTBODY.replace("$tokenValue", token);
        textBodyWithToken = textBodyWithToken.replace("$firstName", firstName);

        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(email))
                .withMessage(new Message()
                        .withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData(htmlBodyWithToken))
                                .withText(new Content().withCharset("UTF-8").withData(textBodyWithToken)))
                        .withSubject(new Content().withCharset("UTF-8").withData(PASSWORD_RESET_SUBJECT)))
                .withSource(FROM);

        SendEmailResult result = client.sendEmail(request);
        if (result != null && (result.getMessageId() != null && !result.getMessageId().isEmpty())) {
            returnValue = true;
        }
        return returnValue;
    }
}
