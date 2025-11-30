import { HouseholdMember } from "./householdMember";
import { ExpenditureCategory } from "./expenditureCategory";
import { Account } from "./account";

export class Household {
    declare public id:number;

    name!: string;
    creator?: HouseholdMember;
    created!: Date;
    expenditureCategories!: ExpenditureCategory[];
    accounts!: Account[];

    public toString(): string {
        return this.id + ', ' + this.name.toString();
    }    
}