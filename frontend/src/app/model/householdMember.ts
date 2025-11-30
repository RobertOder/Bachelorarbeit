import { Account } from "./account";
import { Household } from "./household";

export class HouseholdMember {
    declare public id:number;

    name!: string;
    firstname!: string;
    email!: string;
    phone!: string;
    address!: string;
    city!: string;
    state!: string;
    zip!: string;
    country!: string;
    password!: string;
    created!: Date;
    accounts?: Account[];
    households?: Household[];

    public toString(): string {
        return this.id + ', ' + this.name.toString();
    }    
}