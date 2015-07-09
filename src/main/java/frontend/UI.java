package frontend;

public abstract class UI {

	protected String[] args;

	public abstract void execute();
	
	public String[] getArgs() {
		return this.args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	
	
	
}
