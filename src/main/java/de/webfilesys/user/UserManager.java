package de.webfilesys.user;

import java.util.*;

public interface UserManager 
{

    /**
     * Returns the list of the userids of all registered users.
     *
     * @return a vector of Strings (userids)
     */
    public Vector getListOfUsers();

    /**
     * Returns the e-mail addresses of all users of role "admin"
     *
     * @return a vector of Strings (e-mail addresses)
     */
    public Vector getAdminUserEmails();

    /**
     * Returns the e-mail addresses of all users of a given role
     *
     * @param receiverRole the role of the users ("admin", "user" or "webspace")
     * @return a vector of Strings (e-mail addresses)
     */
    public Vector getMailAddressesByRole(String receiverRole);

    /**
     * Returns the e-mail addresses of all users
     *
     * @return a vector of Strings (e-mail addresses)
     */
    public Vector getAllMailAddresses();

    /**
     * Create a user with the given userid.
     *
     * @param userId the userId (=login name) of the new user
     * @return true if the user could be created
     */
    public boolean addUser(String userId);

    /**
     * Remove the user with the given userId from the database.
     *
     * @param userId the userid (=login name) of the user to be deleted
     * @return true if the user could be deleted
     */
    public boolean removeUser(String userId);

    /**
     * Checks if the user with the given userId exists in the database.
     *
     * @param userId the userid of the user
     * @return true if the user exists
     */
    public boolean userExists(String userId);


    /**
     * When a user publishes a complete folder tree, a virtual user is created
     * that has read-only access and can be used for login by guests. The path
     * of the base folder is used as document root for the virtual user.
     * The virtual user exists only for a limited time. The generated userid of
     * the virtual user must be unique. It is the responsibility of the
     * InvitationManager to remove expired virtual users.
     *
     * @param realUser the userid of the user that owns this folder
     * @param docRoot the path of the published folder, used as document root
     * @param expDays the time to live of the user in days
     * @return the generated unique userid of the virtual user created
     */
    public String createVirtualUser(String realUser,String docRoot,String role,int expDays);

    /**
     * The user type is used to flag virtual users that are created for
     * published folder trees.
     *
     * @param userId the userid of the user
     * @param type the type of the user (for example "virtual")
     * @return false if the user does not exist, otherwise true     
     */
    public boolean setUserType(String userId,String type);

    /**
     * Get the type of the user.
     *
     * @param userId the userid of the user
     * @return null if the user does not exist, "default" if the user type has not ben set
     */
    public String getUserType(String userId);

    /**
     * Get the first name of the user.
     *
     * @param userId the userid of the user
     * @return the first name of the user
     */
    public String getFirstName(String userId);
      
    /**
     * Set a new first name for an user.
     *
     * @param userId the userid of the user
     * @param newValue the new first name
     */
    public void setFirstName(String userId,String newValue);

    /**
     * Get the last name of the user.
     *
     * @param userId the userid of the user
     * @return the last name of the user
     */
    public String getLastName(String userId);

    /**
     * Set a new last name for an user.
     *
     * @param userId the userid of the user
     * @param newValue the new last name
     */
    public void setLastName(String userId,String newValue);

    /**
     * Get the e-mail address of the user.
     *
     * @param userId the userid of the user
     * @return the e-mail address of the user
     */
    public String getEmail(String userId);

    /**
     * Set a new e-mail address for an user.
     *
     * @param userId the userid of the user
     * @param newValue the new e-mail address
     */
    public void setEmail(String userId,String newValue);

    /**
     * Get the disk quota (bytes) of the user. If no disk quota is
     * defined for the user, this method returns -1.
     *
     * @param userId the userid of the user
     * @return the disk quota of the user (in bytes) or -1 if unlimited
     */
    public long getDiskQuota(String userId);

    /**
     * Set a new disk quota for an user.
     *
     * @param userId the userid of the user
     * @param newValue the new disk quota value (bytes)
     */
    public void setDiskQuota(String userId,long newValue);


    /**
     * Get the number of pictures displayed per page.
     * If not defined the method should return fmweb.thumbnailsPerPage .
     *
     * @param userId the userid of the user
     * @return the page size
     */
    public int getPageSize(String userId);

    /**
     * Set the number of pictures per page to be displayed. 
     *
     * @param userId the userid of the user
     * @param newValue the new page size value
     */
    public void setPageSize(String userId,int newValue);

    /**
     * Get the date of the last login.
     *
     * @param userId the userid of the user
     * @return the date of the last login or null if no record exists
     */
    public Date getLastLoginTime(String userId);

    /**
     * Set the date of the last login. 
     *
     * @param userId the userid of the user
     * @param newValue the new last login date
     */
    public void setLastLoginTime(String userId,Date newValue);

    /**
     * Get the phone number of the user.
     *
     * @param userId the userid of the user
     * @return the phone number of the user
     */
    public String getPhone(String userId);

    /**
     * Set a new phone number for an user.
     *
     * @param userId the userid of the user
     * @param newValue the new phone number
     */
    public void setPhone(String userId,String newValue);

    /**
     * Get the language of the user.
     *
     * @param userId the userid of the user
     * @return the language of the user
     */
    public String getLanguage(String userId);

    /**
     * Set a new language for the user.
     *
     * @param userId the userid of the user
     * @param newValue the new language
     */
    public void setLanguage(String userId,String newValue);

    /**
     * Get the role of the user. Can be "admin", "user" or "webspace".
     *
     * @param userId the userid of the user
     * @return the role of the user
     */
    public String getRole(String userId);

    /**
     * Set a new role for the user. Can be "admin", "user" or "webspace".
     *
     * @param userId the userid of the user
     * @param newRole the new role
     */
    public void setRole(String userId,String newRole);

    /**
     * Checks if the user has read-only access.
     *
     * @param userId the userid of the user
     * @return true if user has read-only access to the file system
     */
    public boolean isReadonly(String userId);
    
    /**
     * Set read-only access to the file system for the user.
     *
     * @param userId the userid of the user
     * @param readonly set to true for read-only access
     */
    public void setReadonly(String userId,boolean readonly);

    /**
     * Get the document root for the user.
     * If the document root is not defined, this method
     * must return a platform-dependend default value. 
     * For Windows this should be "c:\temp" and for UNIX "/tmp".
     *
     * @param userId the userid of the user
     * @return the document root of the user
     */
    public String getDocumentRoot(String userId);

    /**
     * Get the document root for the user in all lowercase letters.
     *
     * @param userId the userid of the user
     * @return the document root of the user (all lowercase letters)
     */
    public String getLowerCaseDocRoot(String userId);

    /**
     * Creates a normalized form of the document root.
     * Implemented in abstract base class UserManagerBase.
     * Do not overwrite it!
     *
     * @param documentRoot the raw document root
     * @return the normalized document root
     */
    public String normalizeDocRoot(String documentRoot);

    /**
     * Set a new document root for the user.
     *
     * @param userId the userid of the user
     * @param newValue a valid filesystem path
     */
    public void setDocumentRoot(String userId,String newValue);

    /**
     * Set a new password for the user.
     *
     * @param userId the userid of the user
     * @param newPasword the new password
     */
    public void setPassword(String userId,String newPassword);

    /**
     * Checks if the given password is valid for the user.
     *
     * @param userId the userid of the user
     * @param password the password to check
     * @return true if the given password is valid
     */
    public boolean checkPassword(String userId,String password);

    /**
     * Checks if the given read-only password is valid for the user.
     *
     * @param userId the userid of the user
     * @param password the password to check
     * @return true if the given read-only password is valid
     */
    public boolean checkReadonlyPassword(String userId,String password);

    /**
     * Set a new read-only password for the user.
     *
     * @param userId the userid of the user
     * @param newPasword the new read-only password
     */
    public void setReadonlyPassword(String userId,String newPassword);

    /**
     * Get the CSS stylesheet assigned to the user.
     *
     * @param userId the userid of the user
     * @return the name of the CSS stylesheet
     */
    public String getCSS(String userId);
 
    /**
     * Assigns a CSS stylesheet to the user.
     *
     * @param userId the userid of the user
     * @param newCSS the name of the new CSS stylesheet
     */
    public void setCSS(String userId,String newCSS);
    
	/**
	 * Get a list of transient user objects for all non-virtual users.
	 */
	public Vector getRealUsers();
    
    /**
     * The user manager thread is being notified before fmweb shutdown.
     * If the user manager is ready for shutdown, it must return true here.
     *
     * @return true if the user manager is ready for shutdown
     */
    public boolean isReadyForShutdown();

}

