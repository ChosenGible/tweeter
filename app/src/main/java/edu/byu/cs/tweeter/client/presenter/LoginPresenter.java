package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;
import edu.byu.cs.tweeter.client.service.UserService;

public class LoginPresenter implements UserService.LoginObserver{
    public LoginPresenter(LoginView view){
        this.view = view;
    }

    private LoginView view;

    //private methods
    private String validateLogin(String alias, String password) {
        if (alias.charAt(0) != '@') {
            return "Alias must begin with @.";
        }
        if (alias.length() < 2) {
            return "Alias must contain 1 or more characters after the @.";
        }
        if (password.length() == 0) {
            return "Password cannot be empty.";
        }
        return null;
    }

    //methods that the view can call on the edu.byu.cs.tweeter.client.presenter
    public void login(String username, String password){
        String validateError = validateLogin(username, password);

        if (validateError != null){
            view.displayErrorMessage(validateError);

        }
        else {
            view.clearErrorMessage();
            view.displayInfoMessage("Logging In...");
            new UserService().login(username, password, this);
        }
    }

    //methods the edu.byu.cs.tweeter.client.presenter can call on the view
    public interface LoginView {
        void displayErrorMessage(String message);
        void clearErrorMessage();

        void displayInfoMessage(String message);
        void clearInfoMessage();

        void navigateToUser(User user);
    }


    //observer methods (something changed in the model)
    @Override
    public void handleLoginSuccess(User user, AuthToken token){
        view.clearInfoMessage();
        view.clearErrorMessage();

        view.displayInfoMessage("Hello " + Cache.getInstance().getCurrUser().getName());

        view.navigateToUser(user);
    }
    @Override
    public void handleLoginFailed(String message){
        view.displayInfoMessage("Failed to login: " + message);
    }
    @Override
    public void handleLoginThrewException(Exception e){
        view.displayInfoMessage("Failed to login because of exception: " + e.getMessage());
    }
}
