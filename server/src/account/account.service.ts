import { Injectable } from '@nestjs/common';
import { Account } from '@prisma/client';
import { PrismaService } from 'src/core/prisma.service';

@Injectable()
export class AccountService {
  constructor(private prismaService: PrismaService) {}

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
}
