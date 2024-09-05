package org.example;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;

/**
 * 프로젝트를 구성하는것은
 * 맴버 리스트
 * 프로젝트 구조
 * 프로젝트 옵션 이다. - 옵션은 우성 생각하지 말고 구현하자
 *
 * 맴버 를 구성하는것은
 * 맴버의 userId값
 * 역할 리스트
 * 그외 예측되지않는 프로젝트에서만 존제하는 해당 유저의 정보
 */

public class ProjectEditor {

    public enum Key{
        NONE,
        ProjectId,
        ProjectName,
        ProjectProfileImage,
        Structure,
        Options,
        Role,
        ;

        @Override
        public String toString() {
            return name();
        }

        public static Key toKey(String str) {
            for (Key key : Key.values()) {
                if (key.name().equalsIgnoreCase(str))
                    return key;
            }
            return NONE;
        }
    }
    public static final int PROJECT_ID_NOT_SETUP = -1;
    private int projectId;
    private String projectName;
    private int profileImageId;
    private boolean isOpen;
    private Map<Integer, Member> memberMap;
    private Map<Integer, Role> roleMap;
    private Map<Integer, Structure> structureMap;

    private Options options;

    public static ProjectEditor GenerateNewProject(String projectName){
        return new ProjectEditor(projectName);
    }
    public static ProjectEditor GetProjectEditor(int projectId){
        Map<String, Object> defaultData = MysqlManager.Instance().getDefaultProjectData(projectId);
        if(defaultData == null){
            return null;
        }

        ProjectEditor projectEditor = new ProjectEditor();
        projectEditor.projectId = projectId;
        projectEditor.projectName = defaultData.get("name").toString();
        projectEditor.profileImageId = Integer.parseInt(defaultData.get("profile_image_id").toString());
        projectEditor.isOpen = (Integer)defaultData.get("is_open") == 1;

        System.out.println("get project editor [" + projectId + "] : " + projectEditor.projectName);

        return projectEditor;
    }

    public static ProjectEditor GetProjectEditorByChannelId(int channelId){
        return GetProjectEditor(Integer.parseInt(
                MysqlManager.Instance().getProjectChannelDataByChannelId(channelId)
                        .get("project_id").toString()
        ));

    }

    private ProjectEditor(String projectName){
        this();
        this.projectId = MysqlManager.Instance().createProject(projectName, Util.NOT_SETUP_I, isOpen);
        this.projectName = projectName;
        this.profileImageId = Util.NOT_SETUP_I;
    }
    private ProjectEditor(){
        memberMap = new HashMap<>();
        roleMap = new HashMap<>();
        structureMap = new HashMap<>();
        isOpen = false;
    }

    public ProjectEditor loadRoleData(){
        roleMap.clear();
        JSONObject roleJsonObject = MysqlManager.Instance().getRoleJsonObject(this.projectId);
        Iterator<String> roleIdIterator = roleJsonObject.keys();
        while (roleIdIterator.hasNext()){
            String roleId = roleIdIterator.next();
            Role role = Role.ReadRole(Integer.parseInt(roleId), roleJsonObject.getJSONObject(roleId));
            roleMap.put(role.roleId, role);
        }
        return this;
    }

    public void setIsOpen(boolean isOpen){
        this.isOpen = isOpen;
    }

    public JSONObject getDefaultData(){
        Map<String, Object> projectMap = MysqlManager.Instance().getDefaultProjectData(projectId);
        if(projectMap == null) {
            return null;
        }

        this.projectName = projectMap.get("name").toString();
        this.profileImageId = Integer.parseInt(projectMap.get("profile_image_id").toString());

        return new JSONObject()
                .put(Key.ProjectId.toString(), projectMap.get("id"))
                .put(Key.ProjectName.toString(), projectMap.get("name").toString())
                .put(Key.ProjectProfileImage.toString(), projectMap.get("profile_image_id"));
    }
    public String getProjectName(){
        return this.projectName;
    }

    public int roleMapSize(){
        return roleMap.size();
    }
    public Role addRole(int roleId, String roleName){
        return addNewRole(new Role(roleId, roleName));
    }

    public Role addNewRole(Role role){
        roleMap.put(role.roleId, role);
        return role;
    }

    public Role getRoleInRoleMap(String roleName){
        for (Role role: roleMap.values()) {
            if( role.roleName.equals(roleName)){
                return role;
            }
        }
        return null;
    }
    public JSONObject roleDataToJsonObject(){
        JSONObject result = new JSONObject();
        for (Role role: roleMap.values()) {
            result.put(role.roleIdToString(), role.getRollToJsonObject());
        }
        return result;
    }

    public void applyRoleUpdate(){
        MysqlManager.Instance().updateRoleInProject(this.projectId, roleDataToJsonObject());
    }


    private ProjectEditor loadMemberData(){
        memberMap.clear();
        List<Map<String, Object>> memberData = MysqlManager.Instance().getMembersJsonObjectInProject(this.projectId);
        for (Map<String, Object> element: memberData) {
            int userId = Integer.parseInt(element.get("user_id").toString());
            String memberId = element.get("member_id").toString();
            JSONArray roleList = new JSONArray(element.get("role_list").toString());
            memberMap.put(
                    userId,
                    Member.ReadMember(projectId, userId, memberId, roleList)
            );
        }
        return this;
    }

    public Member addMember(int userId){
        Member member = new Member(this.projectId, userId);
        memberMap.put(userId, member);
        return member;
    }

    public Member addMember(Member member){
        memberMap.put(member.userId, member);
        return member;
    }

    public ProjectEditor addRoleToMember(int userId, String roleName){
        addRoleToMember(getMember(userId), roleName);
        return this;
    }

    public ProjectEditor addRoleToMember(Member member, String roleName){
        roleMap.values().stream()
                .filter(role -> role.roleName.equals(roleName))
                .findFirst()
                .map(role -> member.addRole(role.roleId));
        return this;
    }
    public ProjectEditor addRoleToMember(Member member, int roleId){
        if(roleMap.containsKey(roleId)){
            member.addRole(roleId);
        }
        return this;
    }


    public Member getMember(int memberId){
        return memberMap.getOrDefault(memberId, null);
    }

    public void applyAllMember(){
        for (Member member: memberMap.values() ) {
            member.apply();
        }
    }

    //structure 부분

    public ProjectEditor loadStructure(){
        JSONObject structureData = MysqlManager.Instance().getStructureByProjectId(projectId);
        Iterator<String>keys = structureData.keys();
        while (keys.hasNext()){
            String key = keys.next();
            JSONObject jsonObject = structureData.getJSONObject(key);
            Structure s = new Structure(jsonObject);
            this.structureMap.put(s.structureId, s);
        }
        return this;
    }

    public Structure addStructure(String structureName){
        int structureId = (int)getOptions().getOptionValue(Options.OptionKey.LastStructureId, 1);

        Structure structure = new Structure(++structureId, structureName, new HashSet<>());
        this.structureMap.put(structureId, structure);

        options.setOptionValue(Options.OptionKey.LastStructureId, structureId).apply();
        return structure;
    }

    public Structure getStructure(int structureId) {
        return this.structureMap.get(structureId);
    }
    public Structure getStructureByElementId(int elementId) {
        return this.structureMap.values().stream()
                .filter(structure -> structure.elements.contains(elementId))
                .findFirst().get();
    }
    public ProjectEditor deleteStructure(int structureId){
        this.structureMap.remove(structureId);
        return this;
    }

    public JSONObject structureToJsonObject(){
        JSONObject result = new JSONObject();
        for (Map.Entry<Integer, Structure> entry : this.structureMap.entrySet()) {
            result.put(String.valueOf(entry.getKey()), entry.getValue().toJsonObject());
        }
        return result;
    }

    public void applyStructure(){
        if(structureMap != null){
            MysqlManager.Instance().updateStructureById(projectId, this.structureToJsonObject());
        }
    }

    public Options loadOptions(){
        return this.options = new Options(this.projectId);
    }

    public Options getOptions(){
        if(this.options == null){
            return loadOptions();
        }
        return this.options;
    }

    public static class Member {

        public static Member ReadMember(int projectId, int userId) {
            return new Member(projectId, userId);
        }
        public static Member ReadMember(int projectId, int userId, String memberId, JSONArray roleList){
            return new Member(projectId, userId, memberId).setRolesList(roleList);
        }

        public static String toMemberId(int projectId, int userId) {
            return projectId + "-" + userId;
        }

        private int projectId;
        private int userId;
        private String memberId;
        private HashSet<Integer> rolesList;

        private Member(int projectId, int userId) {
            this(projectId, userId, toMemberId(projectId, userId));
        }
        private Member(int projectId, int userId, String memberId){
            this.projectId= projectId;
            this.userId = userId;
            this.memberId = memberId;
            rolesList = new HashSet<>();
        }

        private Member setRolesList(JSONArray rolesList){
            for(int i = 0; i < rolesList.length(); i++){
                this.rolesList.add(rolesList.getInt(i));
            }
            return this;
        }

        // role이 ProjecEditor에 없을 수도있어 문제가 발생할수있기때문에 직접 Memeber에다가 추가하는걸 막는다.
        private Member addRole(Role role){
            this.rolesList.add(role.roleId);
            return this;
        }
        // role이 ProjecEditor에 없을 수도있어 문제가 발생할수있기때문에 직접 Memeber에다가 추가하는걸 막는다.
        private Member addRole(int roleId){
            this.rolesList.add(roleId);
            return this;
        }

        public Member addProjectToRegister(int projectId) {
            MysqlManager.Instance().addProjectToUserRegister(userId, projectId);
            return this;
        }

        private JSONArray getRoleListToJsonArray(){
            JSONArray result = new JSONArray();
            for (int role: rolesList) {
                result.put(role);
            }
            return result;
        }
        public void apply(){
            MysqlManager.Instance().updateProjectMember(
                    projectId,
                    userId,
                    getRoleListToJsonArray()
            );
        }
        public void registerToUser() {
            if(projectId != PROJECT_ID_NOT_SETUP){
                MysqlManager.Instance().addProjectToUserRegister(userId, projectId);
            }
        }

    }

    public static class Role {
        public enum Attribute {
            NONE,
            ProjectMaster,
            ;


            @Override
            public String toString() {
                return name();
            }

            public static Attribute toAttribute(String str) {
                for (Attribute attribute : Attribute.values()) {
                    if (attribute.name().equalsIgnoreCase(str))
                        return attribute;
                }
                return NONE;
            }
        }

        public enum RoleKey {
            NONE,
            Attributes,
            RoleName,
            RoleId,
            ;


            @Override
            public String toString() {
                return name();
            }

            public static RoleKey toAttribute(String str) {
                for (RoleKey roleKey : RoleKey.values()) {
                    if (roleKey.name().equalsIgnoreCase(str))
                        return roleKey;
                }
                return NONE;
            }
        }

        public static Role ReadRole(int roleId, JSONObject jsonObject){
            Role role = new Role(roleId, jsonObject.getString(RoleKey.RoleName.toString()));
            role.setAttributesList(jsonObject.getJSONArray(RoleKey.Attributes.toString()));
            return role;
        }

        private int roleId;
        private String roleName;
        private List<Attribute> attributesList;

        public Role(int roleId, String roleName){
            this.roleId = roleId;
            this.roleName = roleName;
            attributesList = new ArrayList<>();
        }


        public Role mergeRole(Role other){
            this.attributesList.addAll(other.attributesList);

            return this;
        }

        public Role addAttribute(Attribute attribute){
            attributesList.add(attribute);
            return this;
        }

        public Role removeAttribute(Attribute attribute){
            attributesList.remove(attribute);
            return this;
        }

        public void setAttributesList(JSONArray attributesList){
            for (int i = 0; i < attributesList.length(); i++){
                attributesList.put(Attribute.toAttribute(attributesList.getString(i)));
            }
        }

        private JSONObject getRollToJsonObject(){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(RoleKey.RoleName.toString(), roleName);
            jsonObject.put(RoleKey.Attributes.toString(), getAttributesToJSONArray());

            return jsonObject;
        }

        private JSONArray getAttributesToJSONArray(){
            JSONArray result = new JSONArray();
            for (Attribute attr: attributesList) {
                result.put(attr);
            }
            return result;
        }
        public String roleIdToString(){
            return String.valueOf(roleId);
        }

    }

    public static class Structure {
        public enum StructureKey {
            NONE,
            channels,
            categories,

            structureId,
            structureName,
            elements
            ;

            @Override
            public String toString() {
                return this.name();
            }

            public static StructureKey toKey(String str) {
                for (StructureKey structureKey : StructureKey.values()) {
                    if (structureKey.name().equalsIgnoreCase(str))
                        return structureKey;
                }
                return NONE;
            }
        }

        private int structureId;
        private String structureName;
        private Set<Integer> elements;

        private Structure(JSONObject jsonObject){
            this.structureId = jsonObject.getInt(StructureKey.structureId.toString());
            this.structureName = jsonObject.getString(StructureKey.structureName.toString());

            JSONArray elementsJson = jsonObject.getJSONArray(StructureKey.elements.toString());
            this.elements = new HashSet<>();
            for(int i = 0; i < elementsJson.length(); ++i){
                this.elements.add(elementsJson.getInt(i));
            }
        }
        public int getStructureId() {
            return structureId;
        }

        public Structure addElement(Integer elementId){
            elements.add(elementId);
            return this;
        }
        public Structure removeElement(Integer elementId){
            elements.remove(elementId);
            return this;
        }
        private Structure(int structureId, String structureName, Set<Integer> elements) {
            this.structureId = structureId;
            this.structureName = structureName;
            this.elements = elements;
        }


        public JSONObject toJsonObject() {
            return new JSONObject()
                    .put(StructureKey.structureId.toString(), this.structureId)
                    .put(StructureKey.structureName.toString(), this.structureName)
                    .put(StructureKey.elements.toString(), new JSONArray(elements));
        }

        public Structure setName(String structureName ){
            this.structureName = structureName;
            return this;
        }
    }

    public static class Options {
        public enum OptionKey {
            NONE,
            LastStructureId,
            ;

            @Override
            public String toString() {
                return this.name();
            }

            public static OptionKey toKey(String str) {
                for (OptionKey optionKey : OptionKey.values()) {
                    if (optionKey.name().equalsIgnoreCase(str))
                        return optionKey;
                }
                return NONE;
            }
        }
        private int projectId;
        private Map<OptionKey, Object> data;

        private Options(int projectId){
            this.projectId = projectId;
            data = new HashMap<>();

            JSONObject jsonObject = MysqlManager.Instance().getOptionsByProjectId(this.projectId);
            Iterator<String> iterator = jsonObject.keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                data.put(OptionKey.toKey(key), jsonObject.get(key));
            }
        }


        public Object getOptionValue(OptionKey optionKey, Object defaultValue) {
            return data.getOrDefault(optionKey, defaultValue);
        }

        public Options setOptionValue(OptionKey optionKey, Object value){
            data.put(optionKey, value);
            return this;
        }
        public JSONObject dataToJSONObject(){
            JSONObject result = new JSONObject();
            for (Map.Entry<OptionKey, Object> entry: data.entrySet()) {
                result.put(entry.getKey().toString(), entry.getValue());
            }
            return result;
        }

        public void apply(){
            MysqlManager.Instance().updateOptionsByProjectId(projectId, dataToJSONObject());
        }
    }
}
