### Setup

Put this plugin in your mods folder.


### Usage

```java
// Find the mod
LoadedMod mod = (LoadedMod)Vars.mods.list().find((m) -> {
    return m.main != null && m.main.getClass().getSimpleName().equals("PacketInterceptor");
});

if (mod != null) {
    try {

        // Get the methods
        Class<?> cls = mod.main.getClass();
        Method getRead = cls.getDeclaredMethod("getRead");
        Method getType = cls.getDeclaredMethod("getType");
        Method getPlayer = cls.getDeclaredMethod("getPlayer");
        Method setOverwrite = cls.getDeclaredMethod("setOverwrite", Boolean.TYPE);

        // There will be an Event sent by my plugin, you may use that as trigger.
        Events.on(cls, (e) -> {
            try {

                // Get values from plugin
                Reads read = (Reads)getRead.invoke(mod);
                int type = (Integer)getType.invoke(mod);
                Player player = (Player)getPlayer.invoke(mod);
                if (read == null || type == -1 || player == null) {
                    Log.info(String.format("Packet data missing, aborted. [%s, %s, %s]", new Object[]{read, type, player}));
                    return;
                }

                // Check if it is the right packet type
                if (type == 38){ // 38: requestItem packet

                    // Read Values
                    Building build = TypeIO.readBuilding(read);
                    Item item = TypeIO.readItem(read);
                    int amount = read.i();

                    // Process with the values
                    Log.info(String.format("[white]%s[white] just requested [accent]%s[] [%s]%s[] from [accent]%s[]", 
                            player.name, amount, item.color.toString(), item.name, build.block.name));
                }

                // Set Overwrite to true to overwrite the original process
//                setOverwrite.invoke(mod, true);
            } catch (Exception ee) {
                ee.printStackTrace();
            }
        });
        Log.info("Packet interceptor(s) registered.");
    } catch (Exception e) {
        Log.warn("An error has occurred while registering packet interceptor(s).");
        e.printStackTrace();
    }
}
```
