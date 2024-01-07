package org.sword;

import java.io.Serializable;

/**
 * @author chuan
 * @since 2023/12/31
 */
public class UserDto implements Serializable {

    private String userId;
    private String name;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String username) {
        this.name = username;
    }
}
