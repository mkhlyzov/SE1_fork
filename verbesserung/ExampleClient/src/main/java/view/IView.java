package view;

import logic.GameHelper;

public interface IView {

    void render(GameHelper gameHelper);

    void printGameResult(boolean won);
}
