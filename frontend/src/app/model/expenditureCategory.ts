import { Expenditure } from "./expenditure";
import { Household } from "./household";

export class ExpenditureCategory {
    declare public id:number;

    name!: string;
    upperLimit!: number;
    household!: Household;
    expenditures?: Expenditure[];

    public toString(): string {
        return this.id + ', ' + this.name.toString();
    }    
}