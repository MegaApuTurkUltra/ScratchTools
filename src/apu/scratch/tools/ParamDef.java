/**
 * 
 */
package apu.scratch.tools;

/**
 * @author MegaApuTurkUltra
 */
public class ParamDef {
	String name;
	String desc;
	boolean optional;
	ParamType type;
	
	public ParamType getType(){
		return type;
	}

	public String getName() {
		return name;
	}

	public String getDesc() {
		return desc;
	}

	public boolean isOptional() {
		return optional;
	}

	public ParamDef(String name, String desc, boolean optional, ParamType type) {
		this.name = name;
		this.desc = desc;
		this.optional = optional;
		this.type = type;
	}
	
	public boolean equals(Object other){
		if(other instanceof ParamDef){
			return ((ParamDef) other).getName().equals(getName());
		} else return false;
	}
}
