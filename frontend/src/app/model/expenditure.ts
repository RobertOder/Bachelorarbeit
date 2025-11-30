import { Account } from "./account";
import { ExpenditureCategory } from "./expenditureCategory";
import { ReceiptCopy } from "./receiptCopy";

export class Expenditure {
    declare public id:number;

    description!: string;
    article!: string[];
    date!: Date;
    amount!: number;
    currency!: string;
    recurring!: boolean;
    recurringInterval!: string; // ToDo Enum
    account!: Account;
    expenditureCategory!: ExpenditureCategory;
    receiptCopies!: ReceiptCopy[];

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
