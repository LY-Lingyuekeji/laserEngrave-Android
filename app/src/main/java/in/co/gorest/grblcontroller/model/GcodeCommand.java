package in.co.gorest.grblcontroller.model;

import in.co.gorest.grblcontroller.util.GcodePreprocessorUtils;
import in.co.gorest.grblcontroller.util.GrblUtils;

public class GcodeCommand {

    private String command;
    private String comment;
    private Integer size;
    private Boolean hasRomAccess;

    public GcodeCommand(){}

    public GcodeCommand(String command) {
        this.command = command;
        this.parseCommand();
    }

    public void setCommand(String command){
        this.command = command;
        this.parseCommand();
    }

    private void parseCommand(){
        this.comment = GcodePreprocessorUtils.parseComment(command);
        if(this.getHasComment()) this.command = GcodePreprocessorUtils.removeComment(command);
        this.command = GcodePreprocessorUtils.removeWhiteSpace(command);
        this.size = this.command.length() + 1;
        this.hasRomAccess = GrblUtils.hasRomAccess(this.command);
    }

    public String getCommandString() {
        return this.command;
    }

    private boolean getHasComment() {
        return this.comment != null && this.comment.length() != 0;
    }

    public Boolean getHasRomAccess(){ return this.hasRomAccess; }
    public void setHasRomAccess(Boolean hasRomAccess) { this.hasRomAccess = hasRomAccess; }
    public Integer getSize(){ return this.size; }


}
