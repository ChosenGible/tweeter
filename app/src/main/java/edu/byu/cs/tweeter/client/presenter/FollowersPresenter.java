package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.client.service.FollowService;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowersPresenter implements UserService.GetUserObserver, FollowService.GetFollowObserver {

    private final FollowersView view;
    private final User user;
    private final AuthToken authToken;

    private static final String LOG_TAG = "FollowersPresenter";
    private User lastFollower;
    private boolean hasMorePages = true;
    private boolean isLoading = false;

    public static final int PAGE_SIZE = 10;
    @Override
    public void handleGetUserSuccess(User user) {
        view.displayMessage("Getting user's profile...");
        view.navigateToUser(user);
    }

    @Override
    public void handleGetUserFailed(String message) {
        String errorMessage = "Open follower because of error: " + message;
        Log.e(LOG_TAG, errorMessage);
        view.displayMessage(errorMessage);
    }

    @Override
    public void handleGetUserThrewException(Exception e) {
        String errorMessage = "Open follower because of exception: " + e.getMessage();
        Log.e(LOG_TAG, errorMessage, e);
        view.displayMessage(errorMessage);
    }

    @Override
    public void handleSuccess(List<User> followers, boolean hasMorePages) {
        setLastFollower((followers.size() > 0) ? followers.get(followers.size() - 1) : null);
        setHasMorePages(hasMorePages);

        view.setLoading(false);
        view.addItems(followers);
        setLoading(false);
    }

    @Override
    public void handleFailure(String message) {
        String errorMessage = "Failed to retrieve followers: " + message;
        Log.e(LOG_TAG, errorMessage);

        view.setLoading(false);
        view.displayMessage(errorMessage);
        setLoading(false);
    }

    @Override
    public void handleException(Exception ex) {
        String errorMessage = "Failed to retrieve followers because of exception: " + ex.getMessage();
        Log.e(LOG_TAG, errorMessage, ex);

        view.setLoading(false);
        view.displayMessage(errorMessage);
        setLoading(false);
    }

    public interface FollowersView{
        void navigateToUser(User user);
        void displayMessage(String message);
        void setLoading(boolean value);
        void addItems(List<User> newUsers);
    }

    public FollowersPresenter(FollowersView view, User user, AuthToken authToken){
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }

    public User getLastFollower(){
        return lastFollower;
    }

    private void setLastFollower(User lastFollowee){
        this.lastFollower = lastFollowee;
    }

    public boolean isLoading(){
        return isLoading;
    }

    private void setLoading(Boolean value){
        isLoading = value;
    }

    public boolean hasMorePages(){
        return hasMorePages;
    }

    private void setHasMorePages(Boolean value){
        hasMorePages = value;
    }

    public void loadMoreItems(){
        if (!isLoading() && hasMorePages()){
            setLoading(true);
            view.setLoading(true);

            getFollowing(authToken, user, PAGE_SIZE, lastFollower);
        }
    }

    public void getFollowing(AuthToken authToken, User user, int limit, User lastFollowee) {
        getFollowersService().getFollowers(authToken, user, limit, lastFollowee, this);
    }

    public FollowService getFollowersService(){
        return new FollowService();
    }

    public void onSelectedUser(String alias){
        new UserService().getUser(authToken, alias, this);
    }
}
