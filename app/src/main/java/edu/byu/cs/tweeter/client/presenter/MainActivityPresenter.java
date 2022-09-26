package edu.byu.cs.tweeter.client.presenter;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.byu.cs.tweeter.client.backgroundTask.LogoutTask;
import edu.byu.cs.tweeter.client.cache.Cache;
import edu.byu.cs.tweeter.client.service.FollowService;
import edu.byu.cs.tweeter.client.service.StatusService;
import edu.byu.cs.tweeter.client.service.UserService;
import edu.byu.cs.tweeter.client.view.main.MainActivity;
import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class MainActivityPresenter implements StatusService.NewStatusObserver, StatusService.CreatedStatusObserver,
        FollowService.IsFollowerObserver, FollowService.FollowObserver, FollowService.UnfollowObserver,
        FollowService.GetFollowersCountObserver, FollowService.GetFollowingCountObserver, UserService.LogoutObserver {
    private final MainActivityView view;
    private final User user;
    private final AuthToken authToken;

    private final String LOG_TAG = "Main Activity Presenter";

    @Override
    public void onStatusPosted(String post) {
        view.displayMessage("Posting Status...");

        try {
            new StatusService().createStatus(post, Cache.getInstance().getCurrUser(), getFormattedDateTime(), parseURLs(post), parseMentions(post), this);
        } catch (Exception ex) {
            Log.e(LOG_TAG, ex.getMessage(), ex);
            view.displayMessage("Failed to post the status because of exception: " + ex.getMessage());
        }
    }

    @Override
    public void handleStatusSuccess() {
        view.displayMessage("Successfully Posted!");
    }

    @Override
    public void handleStatusFailure(String message) {
        Log.e(LOG_TAG, "Failed to post status: " + message);
        view.displayMessage("Failed to post status: " + message);
    }

    @Override
    public void handleStatusException(Exception ex) {
        Log.e(LOG_TAG, "Failed to post status because of exception: " + ex.getMessage());
        view.displayMessage("Failed to post status because of exception: " + ex.getMessage());

    }

    @Override
    public void handleIsFollowerSuccess(boolean isFollower) {
        view.updateFollowButton(isFollower);
    }

    @Override
    public void handleIsFollowerFailure(String message) {
        Log.e(LOG_TAG, "Failed to determine following relationship: " + message);
        view.displayMessage("Failed to determine following relationship: " + message);
    }

    @Override
    public void handleIsFollowerException(Exception ex) {
        Log.e(LOG_TAG, "Failed to determine following relationship because of exception: " + ex.getMessage());
        view.displayMessage("Failed to determine following relationship because of exception: " + ex.getMessage());
    }

    @Override
    public void handleUnfollowSuccess() {
        updateSelectedUserFollowingAndFollowers();
        view.updateFollowButton(false);
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleUnfollowFailure(String message) {
        Log.e(LOG_TAG, "Failed to unfollow: " + message);
        view.displayMessage("Failed to unfollow: " + message);
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleUnfollowException(Exception ex) {
        Log.e(LOG_TAG, "Failed to unfollow because of exception: " + ex.getMessage());
        view.displayMessage("Failed to unfollow because of exception: " + ex.getMessage());
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleFollowSuccess() {
        updateSelectedUserFollowingAndFollowers();
        view.updateFollowButton(true);
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleFollowFailure(String message) {
        Log.e(LOG_TAG, "Failed to follow: " + message);
        view.displayMessage("Failed to follow: " + message);
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleFollowException(Exception ex) {
        Log.e(LOG_TAG, "Failed to follow because of exception: " + ex.getMessage());
        view.displayMessage("Failed to follow because of exception: " + ex.getMessage());
        view.setFollowButtonEnabled(true);
    }

    @Override
    public void handleFollowersCountSuccess(int count) {
        view.updateFollowersCount(count);
    }

    @Override
    public void handleFollowersCountFailure(String message) {
        Log.e(LOG_TAG, "Failed to get followers count: " + message);
        view.displayMessage("Failed to get followers count: " + message);
    }

    @Override
    public void handleFollowersCountException(Exception ex) {
        Log.e(LOG_TAG, "Failed to get followers count because of exception: " + ex.getMessage());
        view.displayMessage("Failed to get followers count because of exception: " + ex.getMessage());
    }

    @Override
    public void handleFollowingCountSuccess(int count) {
        view.updateFollowingCount(count);
    }

    @Override
    public void handleFollowingCountFailure(String message) {
        Log.e(LOG_TAG, "Failed to get following count: " + message);
        view.displayMessage("Failed to get following count: " + message);
    }

    @Override
    public void handleFollowingCountException(Exception ex) {
        Log.e(LOG_TAG, "Failed to get following count because of exception: " + ex.getMessage());
        view.displayMessage("Failed to get following count because of exception: " + ex.getMessage());
    }

    @Override
    public void handleLogoutSuccess() {
        view.logoutUser();
    }

    @Override
    public void handleLogoutFailure(String message) {
        Log.e(LOG_TAG, "Failed to logout: " + message);
        view.displayMessage("Failed to logout" + message);
    }

    @Override
    public void handleLogoutException(Exception ex) {
        Log.e(LOG_TAG, "Failed to logout because of exception: " + ex.getMessage());
        view.displayMessage("Failed to logout because of exception: " + ex.getMessage());
    }

    public interface MainActivityView {
        void setFollowButtonVisibility(Boolean value);
        void setFollowButtonEnabled(Boolean isEnabled);
        void displayMessage(String message);
        void logoutUser();
        void updateFollowButton(Boolean isFollower);
        void updateFollowingCount(int count);
        void updateFollowersCount(int count);
    }

    public MainActivityPresenter(){
        this.user = null;
        this.authToken = null;
        this.view = null;

        checkUser();
    }

    public MainActivityPresenter(User user, AuthToken authToken, MainActivityView view){
        this.user = user;
        this.authToken = authToken;
        this.view = view;

        checkUser();
    }

    private void checkUser(){
        if (user == null){
            throw new RuntimeException("User not passed to activity");
        }
    }

    public User user(){
        return user;
    }

    public List<String> parseURLs(String post) {
        List<String> containedUrls = new ArrayList<>();
        for (String word : post.split("\\s")) {
            if (word.startsWith("http://") || word.startsWith("https://")) {

                int index = findUrlEndIndex(word);

                word = word.substring(0, index);

                containedUrls.add(word);
            }
        }

        return containedUrls;
    }

    public List<String> parseMentions(String post) {
        List<String> containedMentions = new ArrayList<>();

        for (String word : post.split("\\s")) {
            if (word.startsWith("@")) {
                word = word.replaceAll("[^a-zA-Z0-9]", "");
                word = "@".concat(word);

                containedMentions.add(word);
            }
        }

        return containedMentions;
    }

    public int findUrlEndIndex(String word) {
        if (word.contains(".com")) {
            int index = word.indexOf(".com");
            index += 4;
            return index;
        } else if (word.contains(".org")) {
            int index = word.indexOf(".org");
            index += 4;
            return index;
        } else if (word.contains(".edu")) {
            int index = word.indexOf(".edu");
            index += 4;
            return index;
        } else if (word.contains(".net")) {
            int index = word.indexOf(".net");
            index += 4;
            return index;
        } else if (word.contains(".mil")) {
            int index = word.indexOf(".mil");
            index += 4;
            return index;
        } else {
            return word.length();
        }
    }

    public String getFormattedDateTime() throws ParseException {
        SimpleDateFormat userFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        SimpleDateFormat statusFormat = new SimpleDateFormat("MMM d yyyy h:mm aaa");

        return statusFormat.format(userFormat.parse(LocalDate.now().toString() + " " + LocalTime.now().toString().substring(0, 8)));
    }

    public void checkUserFollowerStatus() {
        if (user.compareTo(Cache.getInstance().getCurrUser()) == 0) {
            view.setFollowButtonVisibility(false);
        } else {
            view.setFollowButtonVisibility(true);

            new FollowService().isFollower(authToken, user, Cache.getInstance().getCurrUser(), this);
        }
    }

    public void onFollowButtonClick(String followButtonText, String compareText){
        view.setFollowButtonEnabled(false);

        if (followButtonText.equals(compareText)) {
            //follow service
            new FollowService().unfollow(authToken, user, this);


            view.displayMessage("Removing " + user.getName() + "...");
        } else {
            //follow service
            new FollowService().follow(authToken, user, this);


            view.displayMessage("Adding " + user.getName() + "...");
        }
    }

    public void onLogoutSelected(){
        view.displayMessage("Logging Out...");

        new UserService().logout(authToken, this);
    }

    public void updateSelectedUserFollowingAndFollowers() {
        //follow service

        new FollowService().updateSelectedUserFollowingAndFollowers(authToken, user, this, this);

    }
}
