package ml.karmaconfigs.locklogin.plugin.bukkit.plugin;

import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;

import java.util.HashMap;
import java.util.Map;

public final class PluginPermission {

    public static Permission reload() {
        Map<String, Boolean> children = new HashMap<>();
        children.put("locklogin.reload.config", true);
        children.put("locklogin.reload.messages", true);

        return new Permission("locklogin.reload", "The permission required to reload configuration/messages files", PermissionDefault.FALSE, children);
    }

    public static Permission reload_config() {
        Permission permission = new Permission("locklogin.reload.config", "The permission required to reload only configuration file", PermissionDefault.FALSE);
        permission.addParent(reload(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission reload_messages() {
        Permission permission = new Permission("locklogin.reload.messages", "The permission required to reload only messages file", PermissionDefault.FALSE);
        permission.addParent(reload(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission applyUpdates() {
        Map<String, Boolean> children = new HashMap<>();
        children.put("locklogin.applyupdates.unsafe", false);

        return new Permission("locklogin.applyupdates", "The permission required to apply LockLogin update", PermissionDefault.FALSE, children);
    }

    public static Permission applyUnsafeUpdates() {
        Permission permission = new Permission("locklogin.applyupdates.unsafe", "The permission required to apply unsafe updates", PermissionDefault.FALSE);
        permission.addParent(applyUpdates(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission setSpawn() {
        return new Permission("locklogin.setspawn", "The permission required to set the login spawn location", PermissionDefault.FALSE);
    }

    public static Permission infoRequest() {
        Map<String, Boolean> children = new HashMap<>();
        children.put("locklogin.info.request", false);
        children.put("locklogin.info.alt", false);

        Permission permission = new Permission("locklogin.info", "The permission required to request player or plugin information", PermissionDefault.OP, children);
        permission.recalculatePermissibles();

        return permission;
    }

    public static Permission playerInfo() {
        Permission permission = new Permission("locklogin.info.request", "The permission required to request other player information", PermissionDefault.FALSE);
        permission.addParent(infoRequest(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission altInfo() {
        Map<String, Boolean> children = new HashMap<>();
        children.put("locklogin.info.alt.alert", true);

        Permission permission = new Permission("locklogin.info.alt", "The permission required to request alt account information", PermissionDefault.FALSE, children);
        permission.addParent(infoRequest(), false);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission altAlert() {
        Permission permission = new Permission("locklogin.info.alt.alert", "The permission required to receive alerts when a player with alt account joins the server", PermissionDefault.FALSE);
        permission.addParent(altInfo(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission account() {
        Map<String, Boolean> children = new HashMap<>();
        children.put("locklogin.account.close", true);
        children.put("locklogin.account.remove", true);
        children.put("locklogin.account.unlock", true);
        children.put("locklogin.account.location", false);

        Permission permission = new Permission("locklogin.account", "The permission required to manage a player account", PermissionDefault.FALSE, children);
        permission.recalculatePermissibles();

        return permission;
    }

    public static Permission closeAccount() {
        Permission permission = new Permission("locklogin.account.close", "The permission required to close a client account", PermissionDefault.FALSE);
        permission.addParent(account(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission delAccount() {
        Permission permission = new Permission("locklogin.account.remove", "The permission required to remove a client account", PermissionDefault.FALSE);
        permission.addParent(account(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission unlockAccount() {
        Permission permission = new Permission("locklogin.account.unlock", "The permission required to unlock a client account", PermissionDefault.FALSE);
        permission.addParent(account(), true);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission locations() {
        Permission permission = new Permission("locklogin.account.location", "The permission required to manage player(s) last location", PermissionDefault.FALSE);
        permission.addParent(account(), false);

        permission.recalculatePermissibles();
        return permission;
    }

    public static Permission forceFA() {
        return new Permission("locklogin.forcefa", "The permission that will force the player run /2fa setup after register", PermissionDefault.FALSE);
    }

    public static Permission alias() {
        return new Permission("locklogin.alias", "The permission to create/destroy or manage aliases", PermissionDefault.FALSE);
    }
}