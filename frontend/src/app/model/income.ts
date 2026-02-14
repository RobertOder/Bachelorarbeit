import { Account } from "./account";

export class Income {
    declare public id:number;

    description!: string;
    date!: Date;
    dateString!: string;
    amount!: number;
    currency!: string;
    recurring!: boolean;
    recurringInterval!: string; // ToDo Enum
    account!: Account;

    //enum RecurringInterval {
    //  DAILY = "DAILY",
    //  WEEKLY = "WEEKLY",
    //  MONTHLY = "MONTHLY",
    //  YEARLY = "YEARLY"
    //}

    public toString(): string {
        return this.id + ', ' + this.description.toString();
    }
}
