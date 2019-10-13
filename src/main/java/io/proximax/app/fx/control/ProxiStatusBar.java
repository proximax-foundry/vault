package io.proximax.app.fx.control;

/**
 *
 * @author thcao
 */
import io.proximax.app.fx.control.skin.ProxiStatusBarSkin;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Skin;
import javafx.scene.image.Image;

/**
 * The status bar control is normally placed at the bottom of a window. It is
 * used to display various types of application status information. This can be
 * a text message, the progress of a task, or any other kind of status (e.g. red
 * / green / yellow lights). By default the status bar contains a label for
 * displaying plain text and a progress bar (see {@link ProgressBar}) for long
 * running tasks. Additional controls / nodes can be placed on the left and
 * right sides (see {@link #getLeftItems()} and {@link #getRightItems()}).
 *
 * <h3>Screenshots</h3> The picture below shows the default appearance of the
 * statusbar. <center><img src="statusbar.png" /></center> <br>
 * The following picture shows the status bar reporting progress of a task.
 * <center><img src="statusbar-progress.png" /></center> <br>
 * The last picture shows the status bar with a couple of extra items added to
 * the left and right. <center><img src="statusbar-items.png" /></center>
 *
 * <h3>Code Sample</h3>
 *
 * <pre>
 * ProxiStatusBar statusBar = new ProxiStatusBar();
 * statusBar.getLeftItems().add(new Button(&quot;Info&quot;));
 * statusBar.setProgress(.5);
 * </pre>
 */
public class ProxiStatusBar extends Control {

    private ProxiStatusBarSkin statusBarSkin = null;
    private EventHandler eventHandler;

    /**
     * Constructs a new status bar control.
     */
    public ProxiStatusBar() {
        getStyleClass().add("status-bar");
    }

    /**
     * Set event handler
     *
     * @param handler
     */
    public void setEventHandler(EventHandler handler) {
        this.eventHandler = handler;
    }

    /**
     * Get event handler
     *
     * @return event handler
     */
    public EventHandler getEventHandler() {
        return eventHandler;
    }

    @Override
    protected Skin<?> createDefaultSkin() {
        statusBarSkin = new ProxiStatusBarSkin(this);
        return statusBarSkin;
    }

    private final StringProperty text = new SimpleStringProperty(this, "text",
            "Status");

    /**
     * The property used for storing the text message shown by the status bar.
     *
     * @return the text message property
     */
    public final StringProperty textProperty() {
        return text;
    }

    /**
     * Sets the value of the {@link #textProperty()}.
     *
     * @param text the text shown by the label control inside the status bar
     */
    public final void setText(String text) {
        textProperty().set(text);
    }

    /**
     * Returns the value of the {@link #textProperty()}.
     *
     * @return the text currently shown by the status bar
     */
    public final String getText() {
        return textProperty().get();
    }

    private final ObjectProperty<Node> graphic = new SimpleObjectProperty<>(
            this, "graphic");

    /**
     * The property used to store a graphic node that can be displayed by the
     * status label inside the status bar control.
     *
     * @return the property used for storing a graphic node
     */
    public final ObjectProperty<Node> graphicProperty() {
        return graphic;
    }

    /**
     * Returns the value of the {@link #graphicProperty()}.
     *
     * @return the graphic node shown by the label inside the status bar
     */
    public final Node getGraphic() {
        return graphicProperty().get();
    }

    /**
     * Sets the value of {@link #graphicProperty()}.
     *
     * @param node the graphic node shown by the label inside the status bar
     */
    public final void setGraphic(Node node) {
        graphicProperty().set(node);
    }

    private final ObservableList<Node> leftItems = FXCollections
            .observableArrayList();

    /**
     * Returns the list of items / nodes that will be shown to the left of the
     * status label.
     *
     * @return the items on the left-hand side of the status bar
     */
    public final ObservableList<Node> getLeftItems() {
        return leftItems;
    }

    private final ObservableList<Node> rightItems = FXCollections
            .observableArrayList();

    /**
     * Returns the list of items / nodes that will be shown to the right of the
     * status label.
     *
     * @return the items on the left-hand side of the status bar
     */
    public final ObservableList<Node> getRightItems() {
        return rightItems;
    }

    private final DoubleProperty progress = new SimpleDoubleProperty(this,
            "progress");

    /**
     * The property used to store the progress, a value between 0 and 1. A
     * negative value causes the progress bar to show an indeterminate state.
     *
     * @return the property used to store the progress of a task
     */
    public final DoubleProperty progressProperty() {
        return progress;
    }

    /**
     * Sets the value of the {@link #progressProperty()}.
     *
     * @param progress the new progress value
     */
    public final void setProgress(double progress) {
        progressProperty().set(progress);
    }

    /**
     * Returns the value of {@link #progressProperty()}.
     *
     * @return the current progress value
     */
    public final double getProgress() {
        return progressProperty().get();
    }

    private final ObjectProperty<ObservableList<String>> nodeItems = new SimpleObjectProperty<ObservableList<String>>();

    /**
     * Returns the list of items / nodes that will be shown to the right of the
     * status label.
     *
     * @return the items on the left-hand side of the status bar
     */
    public final ObjectProperty<ObservableList<String>> nodeItemsProperty() {
        return nodeItems;
    }

    /**
     * Sets the list of nodes.
     *
     * @param nodes, index
     */
    public final void setNodeItems(ObservableList<String> nodes, int idx) {
        nodeItems.set(nodes);
        nodeSelectedItem.set(nodes.get(idx));
    }

    private final StringProperty nodeSelectedItem = new SimpleStringProperty(this, "text",
            "Status");

    /**
     * The property used for storing the node text shown by the status bar.
     *
     * @return the node text property
     */
    public final StringProperty nodeSelectedProperty() {
        return nodeSelectedItem;
    }

    /**
     * Sets the value list of node.
     *
     * @param index
     */
    public final void setNodeSelected(int idx) {
        nodeSelectedItem.set(nodeItems.get().get(idx));
    }

    /**
     * Sets the value list of node.
     *
     * @param newValue
     */
    public final void setNodeSelected(String newValue) {
        nodeSelectedItem.setValue(newValue);
    }

    private final BooleanProperty imageStatusBoolean = new SimpleBooleanProperty(false);

    /**
     * The property used for storing the image status by the status bar.
     *
     * @return the image status property
     */
    public final BooleanProperty imageStatusBooleanProperty() {
        return imageStatusBoolean;
    }

    /**
     * Set image status
     *
     * @param status: if true image is green, otherwise red
     */
    public final void setImageStatus(Boolean status) {
        imageStatusBoolean.set(status);
    }

    private ObjectProperty<Image> CONNECTION_IMAGE_SUCCESS = null;
    private ObjectProperty<Image> CONNECTION_IMAGE_FAILED = null;

    public void setImageSuccess(Image img) {
        CONNECTION_IMAGE_SUCCESS = new SimpleObjectProperty<>(img);
    }

    public ObjectProperty<Image> getImageSuccess() {
        return CONNECTION_IMAGE_SUCCESS;
    }

    public ObjectProperty<Image> getImageFailed() {
        return CONNECTION_IMAGE_FAILED;
    }

    public void setImageFailed(Image img) {
        CONNECTION_IMAGE_FAILED = new SimpleObjectProperty<>(img);
    }

}
