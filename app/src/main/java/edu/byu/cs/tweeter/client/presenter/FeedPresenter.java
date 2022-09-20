package edu.byu.cs.tweeter.client.presenter;

import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.GetFeedTask;
import edu.byu.cs.tweeter.client.service.FeedService;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.client.view.main.feed.FeedFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class FeedPresenter implements FeedService.FeedObserver, UserService.GetUserObserver {
    private final User user;
    private final AuthToken authToken;
    private final FeedView view;

    private boolean hasMorePages = true;
    private boolean isLoading = false;
    private Status lastStatus = null;

    private static final int PAGE_SIZE = 10;
    private static final String LOG_TAG = "FollowersPresenter";

    @Override
    public void handleSuccess(List<Status> statuses, boolean hasMorePages) {
        isLoading = false;
        view.setLoading(false);

        lastStatus = (statuses.size() > 0) ? statuses.get(statuses.size() - 1) : null;

        view.addItems(statuses);
    }

    @Override
    public void handleFailure(String message) {
        String errorMessage = "Failed to retrieve feed: " + message;
        Log.e(LOG_TAG, errorMessage);

        isLoading = false;
        view.setLoading(false);

        view.displayMessage(errorMessage);
    }

    @Override
    public void handleException(Exception ex) {
        String errorMessage = "Failed to retrieve feed: " + ex.getMessage();

        Log.e(LOG_TAG, errorMessage);
        isLoading = false;
        view.setLoading(false);

        view.displayMessage(errorMessage);
    }

    @Override
    public void handleGetUserSuccess(User user) {
        view.displayMessage("Getting user's profile...");
        view.navigateToUser(user);
    }

    @Override
    public void handleGetUserFailed(String message) {
        String errorMessage = "Unable to open user because of error: " + message;
        Log.e(LOG_TAG, errorMessage);
        view.displayMessage(errorMessage);
    }

    @Override
    public void handleGetUserThrewException(Exception e) {
        String errorMessage = "Unable to open user because of exception: " + e.getMessage();
        Log.e(LOG_TAG, errorMessage, e);
        view.displayMessage(errorMessage);
    }

    public interface FeedView{
        void setLoading(boolean value);
        void navigateToUser(User user);
        void navigateToUrl(Uri uri);
        void displayMessage(String message);
        void addItems(List<Status> statuses);
    }

    public FeedPresenter(FeedView view, User user, AuthToken authToken){
        this.view = view;
        this.authToken = authToken;
        this.user = user;
    }


    //methods the view can call
    public User getUser(){
        return user;
    }

    public AuthToken getAuthToken(){
        return authToken;
    }

    public boolean isLoading(){
        return isLoading;
    }

    public boolean hasMorePages(){
        return hasMorePages;
    }

    public void loadMoreItems(){
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoading(true);

            new FeedService().getFeed(authToken, user, PAGE_SIZE, lastStatus, this);
        }
    }

    public void onSelectedUser(String clickable){
        if (clickable.contains("http")){
            Uri uri = Uri.parse(clickable);
            view.navigateToUrl(uri);
        }
        else {
            new UserService().getUser(authToken, clickable, this);
        }
    }
}
