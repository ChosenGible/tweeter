package edu.byu.cs.tweeter.client.presenter;

import edu.byu.cs.tweeter.model.domain.AuthToken;
import edu.byu.cs.tweeter.model.domain.User;

public class StoryPresenter {
    private final StoryView view;
    private final User user;
    private final AuthToken authToken;

    public interface StoryView {

    }

    public StoryPresenter(User user, AuthToken authToken, StoryView view){
        this.view = view;
        this.user = user;
        this.authToken = authToken;
    }
}
