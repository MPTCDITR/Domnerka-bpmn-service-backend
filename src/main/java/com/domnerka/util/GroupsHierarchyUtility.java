package com.domnerka.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.domnerka.contant.ApplicationConst.*;

@UtilityClass
public class GroupsHierarchyUtility {

    private static final Pattern LEADING_SLASHES = Pattern.compile("^/+");
    private static final Pattern TRAILING_SLASHES = Pattern.compile("/+$");

    public static String[] getGroupOfEachPositions(List<String> userGroupList) {
        // check if user have any group
        if (userGroupList.isEmpty()) {
            return new String[0];
        }

        // convert and split userGroup data to each individual group
        String userGroup = userGroupList.get(0);
        userGroup = LEADING_SLASHES.matcher(userGroup).replaceFirst("");
        userGroup = TRAILING_SLASHES.matcher(userGroup).replaceAll("");
        String[] groupList = userGroup.split("/");

        // initialize position list with each of its code
        String[] positionList = {
                MINISTRY_CODE,
                STANDING_SECRETARY_OF_STATE_CODE,
                SECRETARY_OF_STATE_CODE,
                UNDER_SECRETARY_OF_STATE_CODE,
                GENERAL_DEPARTMENT_CODE,
                DEPARTMENT_CODE,
                OFFICE_CODE,
                STAFF_CODE
        };

        // get group of each position
        IntStream.range(0, positionList.length)
                .forEach(i -> positionList[i] = (i == 0 ? "/" : positionList[i - 1] + "/")
                        + getGroupByCode(groupList, positionList[i]));

        // clean positions data
        return Arrays.stream(positionList)
                .map(s -> s.endsWith("null") ? "" : s.replaceAll("/null", ""))
                .toArray(String[]::new);
    }

    /**
     * Gets group by code.
     *
     * @param groupList the group list
     * @param code      the code
     * @return the group by code
     */
    public static String getGroupByCode(String[] groupList, String code) {
        for (String group : groupList) {
            if (group.startsWith(code))
                return group;
        }
        // return null when no group start with code found
        return "null";
    }

    /**
     * Identifies the highest position held by a user from their group memberships.
     *
     * Analyzes group strings to find the user's top-level role within the
     * organization.
     * Matches the last group against predefined position codes. If no exact match,
     * combines the second-to-last and last groups.
     *
     * @param userGroupList User group membership strings.
     * @return Highest-level position string or empty if unrecognized.
     */
    public static String getUserPosition(List<String> userGroupList) {
        if (userGroupList.isEmpty()) {
            return "";
        }

        // convert and split usergroup data to each individual group
        String userGroup = userGroupList.get(0);
        String[] groupList = userGroup.split("/");
        String userPosition = groupList[groupList.length - 1];

        // initialize position list with each of its code
        String[] positionList = {
                MINISTRY_CODE,
                STANDING_SECRETARY_OF_STATE_CODE,
                SECRETARY_OF_STATE_CODE,
                UNDER_SECRETARY_OF_STATE_CODE,
                GENERAL_DEPARTMENT_CODE,
                DEPARTMENT_CODE,
                OFFICE_CODE,
                STAFF_CODE
        };

        for (String s : positionList) {
            if (userPosition.startsWith(s)) {
                return s;
            }
        }

        // if position code not much, get second-to-last position and return with last
        // position
        String higherPosition = groupList[groupList.length - 2];
        String higherPositionCode = "";

        for (String s : positionList) {
            if (higherPosition.startsWith(s)) {
                higherPositionCode = s;
            }
        }

        return higherPositionCode + "_" + groupList[groupList.length - 1];
    }

    /**
     * Removes group codes from the given user group string.
     *
     * This method removes specific group codes from the input user group string
     *
     * @param userGroup the user group string containing group codes
     * @return the modified user group string with group codes removed
     */
    public static String removeGroupCode(String userGroup) {

        // initialize position list with each of its code
        String[] positionList = {
                MINISTRY_CODE,
                STANDING_SECRETARY_OF_STATE_CODE,
                SECRETARY_OF_STATE_CODE,
                UNDER_SECRETARY_OF_STATE_CODE,
                GENERAL_DEPARTMENT_CODE,
                DEPARTMENT_CODE,
                OFFICE_CODE,
                STAFF_CODE
        };

        for (String groupCode : positionList) {
            userGroup = userGroup.replace("/" + groupCode + "_", "/");
        }

        return userGroup;
    }

    public static List<String> getLowestLevelGroups(List<String> groupList) {
        List<String> lowestGroups = new ArrayList<>();

        for (String group : groupList) {
            String[] parts = group.split("/");
            String lowestGroup = parts[parts.length - 1];
            lowestGroups.add(lowestGroup);
        }

        return lowestGroups;
    }
}
