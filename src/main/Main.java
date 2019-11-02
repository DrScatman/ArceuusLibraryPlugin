/*
package main;

import org.rspeer.runetek.adapter.scene.Player;
import org.rspeer.runetek.api.movement.Movement;
import org.rspeer.runetek.api.movement.position.Area;
import org.rspeer.runetek.api.scene.Players;
import org.rspeer.script.Script;

//@ScriptMeta(name = "Arceuus Library", desc = "Gets arceuus", developer = "DrScatman")
public class Main extends Script {

    private int floorLevel;
    private Area centerArea, neArea, nwArea, swArea;

    private void setAreas(int level) {
        centerArea = Area.rectangular(1622, 3817, 1641, 3799, level);
        neArea = Area.rectangular(1638, 3832, 1659, 3813, level);
        nwArea = Area.rectangular(1606, 3832, 1627, 3813, level);
        swArea = Area.rectangular(1606, 3802, 1627, 3782, level);
    }

    @Override
    public int loop() {
        setAreas(floorLevel);

        if (!playerAtLibrary()) {
            Movement.walkTo(centerArea.getCenter());
        }

        return 1000;
    }

    private boolean playerAtLibrary() {
        Player me = Players.getLocal();
        for (int i = 0; i < 3; i ++) {
            setAreas(i);
            if (centerArea.contains(me) || neArea.contains(me)
                    || nwArea.contains(me) || swArea.contains(me)) {
                return true;
            }
        }
        return false;
    }
}
*/
