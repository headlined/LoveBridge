package com.lovebridge.app.classes;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
public class User {
    @Id
    private String id;          // MongoDB document ID
    private String discordId;   // Discord account ID
    private String time;        // Time of execution
    private String user;        // ROBLOX Username (connected account)
    private String command;     // Command being executed
    private String arg;         // Optional: Command arguments (e.g., "goto <player>")

    private int linked;         // The number of accounts linked
    private boolean premium;    // TRUE: Gives premium benefits

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDiscordId() {
        return discordId;
    }

    public void setDiscordId(String id) {
        this.discordId = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getArg() {
        return arg;
    }

    public void setArg(String arg) {
        this.arg = arg;
    }

    public int getLinked() {
        return linked;
    }

    public void setLinked(int linked) {
        this.linked = linked;
    }

    public boolean isPremium() {
        return premium;
    }

    public void setPremium(boolean premium) {
        this.premium = premium;
    }
}
