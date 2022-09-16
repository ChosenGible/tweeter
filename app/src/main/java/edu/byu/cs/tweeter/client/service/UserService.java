package edu.byu.cs.tweeter.client.service;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import androidx.annotation.NonNull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import edu.byu.cs.tweeter.client.backgroundTask.LoginTask;
import edu.byu.cs.tweeter.client.backgroundTask.RegisterTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class UserService {
    public interface LoginObserver {
        void handleLoginSuccess(User user, AuthToken token);
        void handleLoginFailed(String message);
        void handleLoginThrewException(Exception e);
    }

    public void login(String username, String password, LoginObserver observer){
        // Send the login request.
        LoginTask loginTask = new LoginTask(username, password, new LoginHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(loginTask);
    }

    public void register(String firstname, String lastname, String alias, String password, String imageBytes, LoginObserver observer){
        RegisterTask registerTask = new RegisterTask(firstname, lastname, alias, password, imageBytes, new LoginHandler(observer));
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(registerTask);
    }



    /**
     * Message handler (i.e., observer) for LoginTask
     */
    private class LoginHandler extends Handler {
        private LoginObserver observer;

        public LoginHandler(LoginObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(LoginTask.SUCCESS_KEY);
            if (success) {
                User loggedInUser = (User) msg.getData().getSerializable(LoginTask.USER_KEY);
                AuthToken authToken = (AuthToken) msg.getData().getSerializable(LoginTask.AUTH_TOKEN_KEY);

                // Cache user session information
                Cache.getInstance().setCurrUser(loggedInUser);
                Cache.getInstance().setCurrUserAuthToken(authToken);
                observer.handleLoginSuccess(loggedInUser, authToken);
            } else if (msg.getData().containsKey(LoginTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(LoginTask.MESSAGE_KEY);
                observer.handleLoginFailed(message);
            } else if (msg.getData().containsKey(LoginTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(LoginTask.EXCEPTION_KEY);
                observer.handleLoginThrewException(ex);
            }
        }
    }

    /**
     * Message handler (i.e., observer) for RegisterHandler
     */
    private class RegisterHandler extends Handler {
        private LoginObserver observer;

        private RegisterHandler(LoginObserver observer){
            this.observer = observer;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            boolean success = msg.getData().getBoolean(RegisterTask.SUCCESS_KEY);
            if (success) {
                User registeredUser = (User) msg.getData().getSerializable(RegisterTask.USER_KEY);
                AuthToken authToken = (AuthToken) msg.getData().getSerializable(RegisterTask.AUTH_TOKEN_KEY);

                //bad -> view
                //Intent intent = new Intent(getContext(), MainActivity.class);

                Cache.getInstance().setCurrUser(registeredUser);
                Cache.getInstance().setCurrUserAuthToken(authToken);

                //intent.putExtra(MainActivity.CURRENT_USER_KEY, registeredUser);

                //bad -> presenter should signal this, view should display
                //registeringToast.cancel();

                //bad -> presenter to make message, view should display
                //Toast.makeText(getContext(), "Hello " + Cache.getInstance().getCurrUser().getName(), Toast.LENGTH_LONG).show();
                //bad -> view
//                try {
//                    startActivity(intent);
//                } catch (Exception e) {
//                    System.out.println(e.getMessage());
//                }
                observer.handleLoginSuccess(registeredUser, authToken);
            } else if (msg.getData().containsKey(RegisterTask.MESSAGE_KEY)) {
                String message = msg.getData().getString(RegisterTask.MESSAGE_KEY);
                observer.handleLoginFailed(message);
                //bad presenter make message, view should display
                //Toast.makeText(getContext(), "Failed to register: " + message, Toast.LENGTH_LONG).show();
            } else if (msg.getData().containsKey(RegisterTask.EXCEPTION_KEY)) {
                Exception ex = (Exception) msg.getData().getSerializable(RegisterTask.EXCEPTION_KEY);
                observer.handleLoginThrewException(ex);
                //bad -> presenter message, view display
                //Toast.makeText(getContext(), "Failed to register because of exception: " + ex.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
