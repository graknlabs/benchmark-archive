import { OrgsListMembersResponseItem } from "@octokit/rest";

export interface IGlobal extends NodeJS.Global {
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    graknlabsMembers: OrgsListMembersResponseItem[];
}