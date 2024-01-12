import type {Meta, StoryObj} from '@storybook/react';
import {CreateModal} from './createModal';
import React from "react";

const meta: Meta<typeof CreateModal> = {
    component: CreateModal,
};

export default meta;
type Story = StoryObj<typeof CreateModal>;

export const Primary: Story = {
    render: () => <CreateModal  onClose={() => false} onSave={() => false} isLoading={false}/>,
};