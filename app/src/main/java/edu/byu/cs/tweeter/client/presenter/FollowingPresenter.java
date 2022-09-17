package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import java.util.List;

import edu.byu.cs.tweeter.client.service.FollowService;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class FollowingPresenter implements FollowService.GetFollowObserver, UserService.GetUserObserver {

    private static final String LOG_TAG = "FollowingPresenter";
    public static final int PAGE_SIZE = 10;

    private final FollowingView view;
    private final User user;
    private final AuthToken authToken;

    private User lastFollowee;
    private boolean hasMorePages = true;
    private boolean isLoading = false;

    @Override
    public void handleSuccess(List<User> followees, boolean hasMorePages) {
        setLastFollowee((followees.size() > 0) ? followees.get(followees.size() - 1) : null);
        setHasMorePages(hasMorePages);

        view.setLoading(false);
        view.addItems(followees);
        setLoading(false);
    }

    @Override
    public void handleFailure(String message) {
        String errorMessage = "Failed to retrieve followees: " + message;
        Log.e(LOG_TAG, errorMessage);

        view.setLoading(false);
        view.displayErrorMessage(errorMessage);
        setLoading(false);
    }

    @Override
    public void handleException(Exception ex) {
        String errorMessage = "Failed to retrieve followees because of exception: " + ex.getMessage();
        Log.e(LOG_TAG, errorMessage, ex);

        view.setLoading(false);
        view.displayErrorMessage(errorMessage);
        setLoading(false);
    }

    @Override
    public void handleGetUserSuccess(User user) {
        view.displayMessage("Getting user's profile...");
        view.navigateToUser(user);
    }

    @Override
    public void handleGetUserFailed(String message) {
        String errorMessage = "Open followee because of error: " + message;
        Log.e(LOG_TAG, errorMessage);
        view.displayErrorMessage(errorMessage);
    }

    @Override
    public void handleGetUserThrewException(Exception e) {
        String errorMessage = "Open followee because of exception: " + e.getMessage();
        Log.e(LOG_TAG, errorMessage, e);
        view.displayErrorMessage(errorMessage);
    }

    public interface FollowingView{
        void setLoading(boolean value);
        void addItems(List<User> newUsers);
        void displayErrorMessage(String message);
        void displayMessage(String message);
        void navigateToUser(User user);
    }

    public FollowingPresenter(FollowingView view, User user, AuthToken authToken){
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }

    public User getLastFollowee(){
        return lastFollowee;
    }

    private void setLastFollowee(User lastFollowee){
        this.lastFollowee = lastFollowee;
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

            getFollowing(authToken, user, PAGE_SIZE, lastFollowee);
        }
    }

    public void onSelectFollowedUser(String alias){
        new UserService().getUser(authToken, alias, this);
    }

    public void getFollowing(AuthToken authToken, User user, int limit, User lastFollowee) {
        getFollowingService().getFollowees(authToken, user, limit, lastFollowee, this);
    }

    public FollowService getFollowingService(){
        return new FollowService();
    }
}
