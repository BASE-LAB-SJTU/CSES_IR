package CS.model;

public class APIdata {
    String funcFullName;    // function full name including their prefix
    String funcDesc;    // function description
    String familyName;        //function family name

    public String getFuncFullName() {
        return funcFullName;
    }

    public void setFuncFullName(String funcFullName) {
        this.funcFullName = funcFullName;
    }

    public String getFuncDesc() {
        return funcDesc;
    }

    public void setFuncDesc(String funcDesc) {
        this.funcDesc = funcDesc;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

}
