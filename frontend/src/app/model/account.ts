import { HouseholdMember } from "./householdMember";
import { Household } from "./household";
import { Expenditure } from "./expenditure";
import { Income } from "./income";

export class Account {
    declare public id:number;

    name!: string;
    balance!: number;
    currency!: string;
    householdMember!: HouseholdMember;
    household!: Household;
    expenditures?: Expenditure[];
    incomes?: Income[];

    public toString(): string {
        return this.id + ', ' + this.name.toString();
    }    
}