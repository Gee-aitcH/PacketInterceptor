package packetinterceptor;

import arc.Events;
import mindustry.gen.RemoteReadServer;
import mindustry.net.Packets;
import mindustry.net.ValidateException;
import pluginutil.GHPlugin;
import mindustry.net.Packets.*;

import java.util.Arrays;

import static arc.util.Log.debug;
import static mindustry.Vars.*;
import static mindustry.core.NetServer.onDisconnect;
import static pluginutil.PluginUtil.SendMode.info;
import static mindustry.Vars.netServer;

public class PacketInterceptor extends GHPlugin {

    private Object[] packetData;
    private boolean overwrite;

    public PacketInterceptor() {
    }

    @Override
    public void init() {

        net.handleServer(Connect.class, (con, connect) -> {

            packetData = new Object[]{con.player, connect.addressTCP};
            Events.fire(new PIConnect());
            packetData = null;

            if (overwrite) return;
            if (netServer.admins.isIPBanned(connect.addressTCP) || netServer.admins.isSubnetBanned(connect.addressTCP))
                con.kick(KickReason.banned);
        });

        net.handleServer(Disconnect.class, (con, packet) -> {
            if(con.player == null) return;

            packetData = new Object[]{con.player, packet.reason};
            Events.fire(new PIDisconnect());
            packetData = null;

            if (overwrite) return;
            onDisconnect(con.player, packet.reason);
        });

//        net.handleServer(ConnectPacket.class, (con, packet) -> {
//            if(con.address.startsWith("steam:")){
//                packet.uuid = con.address.substring("steam:".length());
//            }
//
//            String uuid = packet.uuid;
//            byte[] buuid = Base64Coder.decode(uuid);
//            CRC32 crc = new CRC32();
//            crc.update(buuid, 0, 8);
//            ByteBuffer buff = ByteBuffer.allocate(8);
//            buff.put(buuid, 8, 8);
//            buff.position(0);
//            if(crc.getValue() != buff.getLong()){
//                con.kick(KickReason.clientOutdated);
//                return;
//            }
//
//            if(netServer.admins.isIPBanned(con.address) || netServer.admins.isSubnetBanned(con.address)) return;
//
//            if(con.hasBegunConnecting){
//                con.kick(KickReason.idInUse);
//                return;
//            }
//
//            Administration.PlayerInfo info = netServer.admins.getInfo(uuid);
//
//            con.hasBegunConnecting = true;
//            con.mobile = packet.mobile;
//
//            if(packet.uuid == null || packet.usid == null){
//                con.kick(KickReason.idInUse);
//                return;
//            }
//
//            if(netServer.admins.isIDBanned(uuid)){
//                con.kick(KickReason.banned);
//                return;
//            }
//
//            if(Time.millis() < netServer.admins.getKickTime(uuid, con.address)){
//                con.kick(KickReason.recentKick);
//                return;
//            }
//
//            if(netServer.admins.getPlayerLimit() > 0 && Groups.player.size() >= netServer.admins.getPlayerLimit() && !netServer.admins.isAdmin(uuid, packet.usid)){
//                con.kick(KickReason.playerLimit);
//                return;
//            }
//
//            Seq<String> extraMods = packet.mods.copy();
//            Seq<String> missingMods = mods.getIncompatibility(extraMods);
//
//            if(!extraMods.isEmpty() || !missingMods.isEmpty()){
//                //can't easily be localized since kick reasons can't have formatted text with them
//                StringBuilder result = new StringBuilder("[accent]Incompatible mods![]\n\n");
//                if(!missingMods.isEmpty()){
//                    result.append("Missing:[lightgray]\n").append("> ").append(missingMods.toString("\n> "));
//                    result.append("[]\n");
//                }
//
//                if(!extraMods.isEmpty()){
//                    result.append("Unnecessary mods:[lightgray]\n").append("> ").append(extraMods.toString("\n> "));
//                }
//                con.kick(result.toString());
//            }
//
//            if(!netServer.admins.isWhitelisted(packet.uuid, packet.usid)){
//                info.adminUsid = packet.usid;
//                info.lastName = packet.name;
//                info.id = packet.uuid;
//                netServer.admins.save();
//                Call.infoMessage(con, "You are not whitelisted here.");
//                Log.info("&lcDo &lywhitelist-add @&lc to whitelist the player &lb'@'", packet.uuid, packet.name);
//                con.kick(KickReason.whitelist);
//                return;
//            }
//
//            if(packet.versionType == null || ((packet.version == -1 || !packet.versionType.equals(Version.type)) && Version.build != -1 && !netServer.admins.allowsCustomClients())){
//                con.kick(!Version.type.equals(packet.versionType) ? KickReason.typeMismatch : KickReason.customClient);
//                return;
//            }
//
//            boolean preventDuplicates = headless && netServer.admins.isStrict();
//
//            if(preventDuplicates){
//                if(Groups.player.contains(p -> p.name.trim().equalsIgnoreCase(packet.name.trim()))){
//                    con.kick(KickReason.nameInUse);
//                    return;
//                }
//
//                if(Groups.player.contains(player -> player.uuid().equals(packet.uuid) || player.usid().equals(packet.usid))){
//                    con.kick(KickReason.idInUse);
//                    return;
//                }
//            }
//
//            packet.name = netServer.fixName(packet.name);
//
//            if(packet.name.trim().length() <= 0){
//                con.kick(KickReason.nameEmpty);
//                return;
//            }
//
//            String ip = con.address;
//
//            netServer.admins.updatePlayerJoined(uuid, ip, packet.name);
//
//            if(packet.version != Version.build && Version.build != -1 && packet.version != -1){
//                con.kick(packet.version > Version.build ? KickReason.serverOutdated : KickReason.clientOutdated);
//                return;
//            }
//
//            if(packet.version == -1){
//                con.modclient = true;
//            }
//
//            Player player = Player.create();
//            player.admin = netServer.admins.isAdmin(uuid, packet.usid);
//            player.con = con;
//            player.con.usid = packet.usid;
//            player.con.uuid = uuid;
//            player.con.mobile = packet.mobile;
//            player.name = packet.name;
//            player.color.set(packet.color).a(1f);
//
//            //save admin ID but don't overwrite it
//            if(!player.admin && !info.admin){
//                info.adminUsid = packet.usid;
//            }
//
//            try{
//                writeBuffer.reset();
//                player.write(outputBuffer);
//            }catch(Throwable t){
//                con.kick(KickReason.nameEmpty);
//                err(t);
//                return;
//            }
//
//            con.player = player;
//
//            //playing in pvp mode automatically assigns players to teams
//            player.team(netServer.assignTeam(player));
//
//            netServer.sendWorldData(player);
//
//            platform.updateRPC();
//
//            Events.fire(new EventType.PlayerConnect(player));
//        });

        net.handleServer(Packets.InvokePacket.class, (con, packet) -> {
            if(con.player == null) return;
            try{
                byte[] bytes = Arrays.copyOf(packet.bytes, packet.bytes.length);

                packetData = new Object[]{packet.reader(), packet.type, con.player};
                Events.fire(new PIInvokePacket());
                packetData = null;

                if (overwrite) return;
                packet.bytes = bytes;
                RemoteReadServer.readPacket(packet.reader(), packet.type, con.player);
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
        log(info, "Invoke packet listener overwritten.");
    }

    public Object[] getPacketData(){
        return packetData;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }

    public static class PIConnect{}
    public static class PIDisconnect{}
//    public static class PIConnectPacket{}
    public static class PIInvokePacket{}
}
