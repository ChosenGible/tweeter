package edu.byu.cs.tweeter.client.presenter;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.ByteArrayOutputStream;
import java.util.Base64;

import edu.byu.cs.client.R;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class RegisterPresenter implements UserService.LoginObserver {
    private RegisterView view;

    public RegisterPresenter(RegisterView view){
        this.view = view;
    }


    // methods that the presenter can call on the view
    public interface RegisterView{
        void displayFirstname(String firstname);
        void displayLastname(String lastname);
        void displayAlias(String alias);
        void displayUploadImage(String imageBytes);
        void displayUploadImage(Uri image);
        void setUploadImageButtonText(String text);
        void displayErrorMessage(String message);
        void displayToastMessage(String message);
        void clearErrorMessage();
        void clearToastMessage();
        void clearFields();
        void navigateToUser(User user);
    }

    // methods that teh presenter can call
    public void register(String firstname, String lastname, String alias, String password, Drawable image){
        String validationResult = validateRegistration(firstname, lastname, alias, password, image);
        if (validationResult == ""){
            //clear old messages
            view.clearErrorMessage();
            view.clearToastMessage();

            //display loading message
            view.displayToastMessage("Registering...");

            //convert image to byte array
            Bitmap bitmap = ((BitmapDrawable)image).getBitmap();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            byte[] imageBytes = bos.toByteArray();

            // Intentionally, Use the java Base64 encoder so it is compatible with M4.
            String imageBytesBase64 = Base64.getEncoder().encodeToString(imageBytes);

            new UserService().register(firstname, lastname, alias, password, imageBytesBase64, this);
        }
        else {
            view.displayErrorMessage(validationResult);
        }
    }

    public void setImageFromUpload(Intent data){
        Uri selectedImage = data.getData();
        view.displayUploadImage(selectedImage);
    }

    // methods called by subject
    @Override
    public void handleLoginSuccess(User user, AuthToken token) {
        view.clearToastMessage();
        view.clearErrorMessage();

        view.displayToastMessage("Hello " + Cache.getInstance().getCurrUser().getName());

        view.navigateToUser(user);
    }

    @Override
    public void handleLoginFailed(String message) {
        view.displayToastMessage("Failed to login: " + message);
    }

    @Override
    public void handleLoginThrewException(Exception e) {
        view.displayToastMessage("Failed to login because of exception: " + e.getMessage());
    }

    //private functions
    private String validateRegistration(String firstname, String lastname, String alias, String password, Drawable image) {
        if (firstname.length() == 0) {
            return "First Name cannot be empty.";
        }
        if (lastname.length() == 0) {
            return "Last Name cannot be empty.";
        }
        if (alias.length() == 0) {
            return "Alias cannot be empty.";
        }
        if (alias.charAt(0) != '@') {
            return "Alias must begin with @.";
        }
        if (alias.length() < 2) {
            return "Alias must contain 1 or more characters after the @.";
        }
        if (password.length() == 0) {
            return "Password cannot be empty.";
        }

        if (image == null) {
            return "Profile image must be uploaded.";
        }
        return "";
    }
}
