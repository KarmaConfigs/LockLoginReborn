package eu.locklogin.api.account;

/*
 * GNU LESSER GENERAL PUBLIC LICENSE
 * Version 2.1, February 1999
 * <p>
 * Copyright (C) 1991, 1999 Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * Everyone is permitted to copy and distribute verbatim copies
 * of this license document, but changing it is not allowed.
 * <p>
 * [This is the first released version of the Lesser GPL.  It also counts
 * as the successor of the GNU Library Public License, version 2, hence
 * the version number 2.1.]
 */

import eu.locklogin.api.account.param.AccountConstructor;
import eu.locklogin.api.account.param.Parameter;
import eu.locklogin.api.account.param.SimpleParameter;
import ml.karmaconfigs.api.common.utils.uuid.UUIDUtil;

import java.io.Serializable;
import java.util.UUID;

/**
 * LockLogin account id
 */
public final class AccountID extends AccountConstructor<AccountID> implements Serializable {

    private final String id;

    /**
     * Initialize the account id
     *
     * @param uniqueId the account uuid
     */
    private AccountID(final UUID uniqueId) {
        id = uniqueId.toString();
    }

    /**
     * Get an account id object from an UUID
     *
     * @param uuid the uuid
     * @return a new account id object
     */
    public static AccountID fromUUID(final UUID uuid) {
        return new AccountID(uuid);
    }

    /**
     * Get an account id object from a string UUID
     *
     * @param stringUUID the string UUID
     * @return a new account id object
     */
    public static AccountID fromString(final String stringUUID) {
        UUID result = UUIDUtil.fromTrimmed(stringUUID);
        assert result != null;

        return new AccountID(result);
    }

    /**
     * Get the account id
     *
     * @return the account id
     */
    public String getId() {
        return id;
    }

    /**
     * Get the parameter of the account parameter
     *
     * @return the account constructor parameter
     */
    @Override
    public Parameter<AccountID> getParameter() {
        return new SimpleParameter<>("accountid", this);
    }

    /**
     * Get a class instance of the account constructor
     * type
     *
     * @return the account constructor type
     */
    @Override
    public Class<? extends AccountID> getType() {
        return AccountID.class;
    }
}
