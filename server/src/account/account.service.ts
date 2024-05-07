import { Injectable, OnModuleInit } from '@nestjs/common';
import { Account } from '@prisma/client';
import { hash } from 'bcrypt';
import { PrismaService } from 'src/core/prisma.service';

@Injectable()
export class AccountService implements OnModuleInit {
  constructor(private prismaService: PrismaService) {}
  async onModuleInit() {
    await this.prismaService.account.deleteMany();
    const AccountData = [];
    for (let i = 1; i <= 10; i++) {
      const account = {
        username: `account${i}`,
        password: (await hash(`account${i}`, 10)).toString(),
      };
      AccountData.push(account);
    }
    await this.prismaService.account.createMany({
      data: AccountData,
    });
  }

  async getUsers(): Promise<Account[]> {
    return await this.prismaService.account.findMany();
  }

  async getUser(username: string): Promise<Account | null> {
    const account = await this.prismaService.account.findFirst({
      where: {
        username: {
          equals: username,
        },
      },
    });

    return account ?? null;
  }

  async addFavoritePharmacie(
    accountId: string,
    pharmacyId: string,
  ): Promise<Account> {
    try {
      // Fetch the account to get its current favorite pharmacies
      const account = await this.prismaService.account.findUnique({
        where: {
          id: accountId,
        },
        include: {
          favoritePharmacies: true, // Include favorite pharmacies to avoid duplications
        },
      });

      if (!account) {
        throw new Error(`Account with ID ${accountId} not found.`);
      }

      // Check if the pharmacy already exists in the account's favorite pharmacies
      const pharmacyExists = account.favoritePharmacies.some(
        (pharmacy) => pharmacy.id === pharmacyId,
      );
      if (pharmacyExists) {
        throw new Error(
          `Pharmacy with ID ${pharmacyId} is already a favorite for this account.`,
        );
      }

      // Update the account by adding the new pharmacy ID to favoritePharmaciesIds array
      const updatedAccount = await this.prismaService.account.update({
        where: {
          id: accountId,
        },
        data: {
          favoritePharmaciesIds: {
            push: pharmacyId,
          },
        },
      });

      console.log(
        `Pharmacy with ID ${pharmacyId} added as a favorite for account ${accountId}.`,
      );
      return updatedAccount;
    } catch (error) {
      console.error('Error adding favorite pharmacy:', error);
      throw error;
    }
  }

  async removeFavoritePharmacy(accountId, pharmacyId): Promise<Account> {
    try {
      // Fetch the account to get its current favorite pharmacies
      const account = await this.prismaService.account.findUnique({
        where: {
          id: accountId,
        },
        include: {
          favoritePharmacies: true, // Include favorite pharmacies to avoid duplications
        },
      });

      if (!account) {
        throw new Error(`Account with ID ${accountId} not found.`);
      }

      // Check if the pharmacy exists in the account's favorite pharmacies
      const pharmacyExists = account.favoritePharmacies.some(
        (pharmacy) => pharmacy.id === pharmacyId,
      );
      if (!pharmacyExists) {
        throw new Error(
          `Pharmacy with ID ${pharmacyId} is not a favorite for this account.`,
        );
      }

      // Update the account by removing the pharmacy ID from favoritePharmaciesIds array
      const updatedAccount = await this.prismaService.account.update({
        where: {
          id: accountId,
        },
        data: {
          favoritePharmaciesIds: {
            set: account.favoritePharmaciesIds.filter(
              (id) => id !== pharmacyId,
            ),
          },
        },
      });

      console.log(
        `Pharmacy with ID ${pharmacyId} removed from favorites for account ${accountId}.`,
      );
      return updatedAccount;
    } catch (error) {
      console.error('Error removing favorite pharmacy:', error);
      throw error;
    }
  }

  async addFlaggedPharmacy(
    accountId: string,
    pharmacyId: string,
  ): Promise<Account> {
    try {
      // Fetch the account to ensure it exists
      const account = await this.prismaService.account.findUnique({
        where: {
          id: accountId,
        },
      });

      if (!account) {
        throw new Error(`Account with ID ${accountId} not found.`);
      }

      // Check if the pharmacy is already flagged by the account
      const isAlreadyFlagged =
        account.flaggedPharmaciesIds.includes(pharmacyId);
      if (isAlreadyFlagged) {
        throw new Error(
          `Pharmacy with ID ${pharmacyId} is already flagged by this account.`,
        );
      }

      // Update the account by adding the pharmacy ID to flaggedPharmaciesIds array
      const updatedAccount = await this.prismaService.account.update({
        where: {
          id: accountId,
        },
        data: {
          flaggedPharmaciesIds: {
            push: pharmacyId,
          },
        },
      });

      console.log(
        `Pharmacy with ID ${pharmacyId} flagged by account ${accountId}.`,
      );
      return updatedAccount;
    } catch (error) {
      console.error('Error adding flagged pharmacy:', error);
      throw error;
    }
  }
}
