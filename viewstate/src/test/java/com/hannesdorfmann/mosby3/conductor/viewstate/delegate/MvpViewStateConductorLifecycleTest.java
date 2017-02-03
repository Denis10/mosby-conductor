package com.hannesdorfmann.mosby3.conductor.viewstate.delegate;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.view.View;
import com.bluelinelabs.conductor.Controller;
import com.hannesdorfmann.mosby3.mvp.MvpPresenter;
import com.hannesdorfmann.mosby3.mvp.MvpView;
import com.hannesdorfmann.mosby3.mvp.lce.MvpLceView;
import com.hannesdorfmann.mosby3.mvp.viewstate.ViewState;
import com.hannesdorfmann.mosby3.mvp.viewstate.lce.AbsParcelableLceViewState;
import com.hannesdorfmann.mosby3.mvp.viewstate.lce.data.ParcelableDataLceViewState;
import com.hannesdorfmann.mosby3.mvp.viewstate.lce.data.RetainingLceViewState;
import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author Hannes Dorfmann
 */

@RunWith(PowerMockRunner.class) @PrepareForTest(Controller.class)
public class MvpViewStateConductorLifecycleTest {

  @Test public void nullPointerOnCreateViewState() {

    MvpPresenter<MvpView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    MvpView mvpView = Mockito.mock(MvpView.class);

    MvpViewStateConductorDelegateCallback<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        callback = Mockito.mock(MvpViewStateConductorDelegateCallback.class);

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.getViewState()).thenReturn(null);
    Mockito.when(callback.createViewState()).thenReturn(null);

    MvpViewStateConductorLifecycleListener<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    try {
      lifecycleListener.preAttach(controller, view);
      Assert.fail("NullPointerException expected");
    } catch (NullPointerException e) {
      Assert.assertEquals(e.getMessage(),
          "ViewState from createViewState() is null in " + callback);
    }
  }

  @Test public void createAndSetNewViewState() {
    MvpPresenter<MvpView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    MvpView mvpView = Mockito.mock(MvpView.class);
    ViewState<MvpView> viewState = Mockito.mock(ViewState.class);

    MvpViewStateConductorDelegateCallback<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        callback = Mockito.mock(MvpViewStateConductorDelegateCallback.class);

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.getViewState()).thenReturn(null);
    Mockito.when(callback.createViewState()).thenReturn(viewState);

    MvpViewStateConductorLifecycleListener<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    lifecycleListener.postCreateView(controller, view);
    lifecycleListener.preAttach(controller, view);

    Mockito.verify(callback, Mockito.times(1)).getViewState();
    Mockito.verify(callback, Mockito.times(1)).createViewState();
    Mockito.verify(callback, Mockito.times(1)).setViewState(viewState);
    Mockito.verify(callback, Mockito.times(1)).onNewViewStateInstance();
  }

  @Test public void restoreViewStateFromBundle() {

    MvpPresenter<ModelView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    ModelView mvpView = Mockito.mock(ModelView.class);

    MvpViewStateConductorDelegateCallback<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        callback = Mockito.spy(SemiFunctionalCallback.class);

    ParcelableDataLceViewState<Model, MvpLceView<Model>> viewState =
        Mockito.spy(new ParcelableDataLceViewState<Model, MvpLceView<Model>>());

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.createViewState())
        .thenReturn(new ParcelableDataLceViewState<Model, ModelView>());

    MvpViewStateConductorLifecycleListener<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Bundle bundle = Mockito.mock(Bundle.class);
    Mockito.when(bundle.getParcelable(AbsParcelableLceViewState.KEY_BUNDLE_VIEW_STATE))
        .thenReturn(viewState);
    viewState.saveInstanceState(bundle);

    lifecycleListener.postCreateView(controller, view);
    lifecycleListener.onRestoreViewState(controller, bundle);
    lifecycleListener.preAttach(controller, view);

    Mockito.verify(callback, Mockito.times(1)).setRestoringViewState(true);
    Mockito.verify(viewState, Mockito.times(1)).apply(Mockito.eq(mvpView), Mockito.eq(false));
    Mockito.verify(callback, Mockito.times(1)).setRestoringViewState(false);
    Mockito.verify(callback, Mockito.times(1)).onViewStateInstanceRestored(false);
  }

  @Test public void dontRestoreViewStateBecauseFirstTimeControllerStarts() {

    MvpPresenter<ModelView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    ModelView mvpView = Mockito.mock(ModelView.class);

    MvpViewStateConductorDelegateCallback<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        callback = Mockito.mock(MvpViewStateConductorDelegateCallback.class);

    ParcelableDataLceViewState<Model, MvpLceView<Model>> viewState =
        Mockito.spy(new ParcelableDataLceViewState<Model, MvpLceView<Model>>());

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.createViewState())
        .thenReturn(new ParcelableDataLceViewState<Model, ModelView>());

    MvpViewStateConductorLifecycleListener<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Bundle bundle = Mockito.mock(Bundle.class);

    lifecycleListener.postCreateView(controller, view);
    // lifecycleListener.onRestoreViewState(controller, bundle); Not called because first time starts
    lifecycleListener.preAttach(controller, view);

    Mockito.verify(viewState, Mockito.never()).apply(Mockito.eq(mvpView), Mockito.anyBoolean());
    Mockito.verify(callback, Mockito.times(1)).createViewState();
    Mockito.verify(callback, Mockito.times(1)).setViewState(Mockito.any(ViewState.class));
  }

  @Test public void dontRestoreViewStateBecauseViewStateInMemory() {
    MvpPresenter<MvpView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    MvpView mvpView = Mockito.mock(MvpView.class);
    ViewState<MvpView> viewState = Mockito.mock(ViewState.class);

    MvpViewStateConductorDelegateCallback<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        callback = Mockito.mock(MvpViewStateConductorDelegateCallback.class);

    MvpViewStateConductorLifecycleListener<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.getViewState()).thenReturn(viewState);

    Bundle bundle = new Bundle();

    lifecycleListener.changingConfigurations = true; // Simulate screen orientation change
    lifecycleListener.postCreateView(controller, view);
    lifecycleListener.onRestoreViewState(controller, bundle);

    Mockito.verify(callback, Mockito.never()).createViewState();
    Mockito.verify(viewState, Mockito.times(1)).apply(Mockito.eq(mvpView), Mockito.eq(true));
  }

  @Test public void saveViewState() {

    MvpPresenter<ModelView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    ModelView mvpView = Mockito.mock(ModelView.class);
    Activity activity = Mockito.mock(Activity.class);

    ParcelableDataLceViewState<Model, ModelView> viewState =
        Mockito.spy(new ParcelableDataLceViewState<Model, ModelView>());

    MvpViewStateConductorDelegateCallback<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        callback = Mockito.spy(new SemiFunctionalCallback(viewState));

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.createViewState())
        .thenReturn(new ParcelableDataLceViewState<Model, ModelView>());

    PowerMockito.when(controller.getActivity()).thenReturn(activity);
    Mockito.when(activity.isChangingConfigurations()).thenReturn(false);

    MvpViewStateConductorLifecycleListener<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Bundle bundle = Mockito.mock(Bundle.class);

    lifecycleListener.onSaveViewState(controller, bundle);
    lifecycleListener.preDestroyView(controller, view);

    Mockito.verify(bundle, Mockito.times(1))
        .putParcelable(Mockito.eq(ParcelableDataLceViewState.KEY_BUNDLE_VIEW_STATE),
            Mockito.eq(viewState));
  }

  @Test public void dontSaveNotRestoreableViewState() {

    MvpPresenter<ModelView> presenter = Mockito.mock(MvpPresenter.class);
    Controller controller = PowerMockito.mock(Controller.class);
    View view = Mockito.mock(View.class);
    ModelView mvpView = Mockito.mock(ModelView.class);
    Activity activity = Mockito.mock(Activity.class);

    RetainingLceViewState<Model, ModelView> viewState =
        Mockito.spy(new RetainingLceViewState<Model, ModelView>());

    MvpViewStateConductorDelegateCallback<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        callback = Mockito.spy(new SemiFunctionalCallback(viewState));

    Mockito.when(callback.getPresenter()).thenReturn(presenter);
    Mockito.when(callback.getMvpView()).thenReturn(mvpView);
    Mockito.when(callback.createViewState())
        .thenReturn(new ParcelableDataLceViewState<Model, ModelView>());

    PowerMockito.when(controller.getActivity()).thenReturn(activity);
    Mockito.when(activity.isChangingConfigurations()).thenReturn(false);

    MvpViewStateConductorLifecycleListener<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Bundle bundle = Mockito.mock(Bundle.class);

    lifecycleListener.onSaveViewState(controller, bundle);
    lifecycleListener.preDestroyView(controller, view);

    Mockito.verifyZeroInteractions(bundle);
  }

  @Test public void nullPointerExceptionOnSaveViewState() {
    Controller controller = PowerMockito.mock(Controller.class);

    MvpViewStateConductorDelegateCallback<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        callback = Mockito.mock(MvpViewStateConductorDelegateCallback.class);

    MvpViewStateConductorLifecycleListener<MvpView, MvpPresenter<MvpView>, ViewState<MvpView>>
        lifecycleListener = new MvpViewStateConductorLifecycleListener<>(callback);

    Mockito.when(callback.getViewState()).thenReturn(null);
    Bundle bundle = new Bundle();

    try {
      lifecycleListener.onSaveViewState(controller, bundle);
      Assert.fail("NullPointerException expected");
    } catch (NullPointerException e) {
      Assert.assertEquals(e.getMessage(), "ViewState from getViewState() is null in " + callback);
    }
  }

  static class Model implements Parcelable {

    public static final Parcelable.Creator<Model> CREATOR = new Parcelable.Creator<Model>() {
      @Override public Model createFromParcel(Parcel source) {
        return new Model();
      }

      @Override public Model[] newArray(int size) {
        return new Model[size];
      }
    };

    @Override public int describeContents() {
      return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {

    }
  }

  interface ModelView extends MvpLceView<Model> {
  }

  static class SemiFunctionalCallback implements
      MvpViewStateConductorDelegateCallback<ModelView, MvpPresenter<ModelView>, ViewState<ModelView>> {

    public ViewState<ModelView> viewState;

    public SemiFunctionalCallback() {
    }

    public SemiFunctionalCallback(ViewState<ModelView> viewState) {
      this.viewState = viewState;
    }

    @Override public ViewState<ModelView> getViewState() {
      return viewState;
    }

    @Override public void setViewState(ViewState<ModelView> viewState) {
      this.viewState = viewState;
    }

    @NonNull @Override public ViewState<ModelView> createViewState() {
      return null;
    }

    @Override public void setRestoringViewState(boolean restoringViewState) {

    }

    @Override public boolean isRestoringViewState() {
      return false;
    }

    @Override public void onViewStateInstanceRestored(boolean instanceStateRetained) {

    }

    @Override public void onNewViewStateInstance() {

    }

    @NonNull @Override public MvpPresenter<ModelView> createPresenter() {
      return null;
    }

    @NonNull @Override public MvpPresenter<ModelView> getPresenter() {
      return null;
    }

    @Override public void setPresenter(@NonNull MvpPresenter<ModelView> presenter) {

    }

    @NonNull @Override public ModelView getMvpView() {
      return null;
    }
  }
}