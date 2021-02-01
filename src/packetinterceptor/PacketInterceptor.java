package packetinterceptor;

import arc.Events;
import arc.util.io.Reads;
import mindustry.gen.Player;
import mindustry.gen.RemoteReadServer;
import mindustry.net.Packets;
import mindustry.net.ValidateException;
import pluginutil.GHPlugin;
import static arc.util.Log.debug;
import static mindustry.Vars.net;

public class PacketInterceptor extends GHPlugin {

    private Reads read;
    private int type;
    private Player player;
    private boolean overwrite;

    public PacketInterceptor() {
        read = null;
        type = -1;
        player = null;
        overwrite = false;
    }

    @Override
    public void init() {
        net.handleServer(Packets.InvokePacket.class, (con, packet) -> {
            if(con.player == null) return;
            try{
                this.read = packet.reader();
                this.type = packet.type;
                this.player = con.player;
                Events.fire(PacketInterceptor.class);
                if (overwrite) RemoteReadServer.readPacket(read, type, player);
                this.read = null;
                this.type = -1;
                this.player = null;
            }catch(ValidateException e){
                debug("Validation failed for '@': @", e.player, e.getMessage());
            }catch(RuntimeException e){
                if(e.getCause() instanceof ValidateException){
                    ValidateException v = (ValidateException) e.getCause();
                    debug("Validation failed for '@': @", v.player, v.getMessage());
                }else{
                    throw e;
                }
            }
        });
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public Reads getRead() {
        return read;
    }

    public int getType() {
        return type;
    }

    public Player getPlayer() {
        return player;
    }
}
