package edu.byu.cs.tweeter.client.presenter;

import android.net.Uri;
import android.util.Log;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.GetStoryTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.service.StoryService;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.client.view.main.story.StoryFragment;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.Status;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter implements StoryService.StoryObserver, UserService.GetUserObserver {
    private final StoryView view;
    private final User user;
    private final AuthToken authToken;

    private boolean hasMorePages = true;
    private boolean isLoading = false;
    private Status lastStatus = null;

    private static final int PAGE_SIZE = 10;
    private static final String LOG_TAG = "StoryPresenter";

    @Override
    public void handleStorySuccess(List<Status> statuses, boolean hasMorePages) {
        isLoading = false;
        view.setLoading(false);

        lastStatus = (statuses.size() > 0) ? statuses.get(statuses.size() - 1) : null;

        view.addItems(statuses);
    }

    @Override
    public void handleStoryFailure(String message) {
        String errorMessage = "Failed to retrieve story: " + message;
        Log.e(LOG_TAG, errorMessage);

        isLoading = false;
        view.setLoading(false);

        view.displayMessage(errorMessage);
    }

    @Override
    public void handleStoryException(Exception ex) {
        String errorMessage = "Failed to retrieve story: " + ex.getMessage();

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
        String errorMessage = "Failed to get user's profile: " + message;
        Log.e(LOG_TAG, errorMessage);
        view.displayMessage(errorMessage);
    }

    @Override
    public void handleGetUserThrewException(Exception e) {
        String errorMessage = "Failed to get user's profile because of exception: " + e.getMessage();
        Log.e(LOG_TAG, errorMessage, e);
        view.displayMessage(errorMessage);
    }

    public interface StoryView {
        void addItems(List<Status> statuses);
        void setLoading(boolean value);
        void displayMessage(String message);
        void navigateToUser(User user);
        void navigateToUri(Uri uri);
    }

    public StoryPresenter(User user, AuthToken authToken, StoryView view){
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }

    public boolean isLoading(){
        return isLoading;
    }

    public boolean hasMorePages(){
        return hasMorePages;
    }

    //presenter
    public void loadMoreItems() {
        if (!isLoading) {   // This guard is important for avoiding a race condition in the scrolling code.
            isLoading = true;
            view.setLoading(true);

            new StoryService().getStory(authToken, user, PAGE_SIZE, lastStatus, this);
        }
    }

    public void onSelectedUser(String clickable){
        if (clickable.contains("http")){
            Uri uri = Uri.parse(clickable);
            view.navigateToUri(uri);
        }
        else {
            new UserService().getUser(authToken, clickable, this);
        }
    }
}
